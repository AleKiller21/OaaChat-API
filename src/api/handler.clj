(ns api.handler
  (:require [compojure.core :refer :all]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [buddy.auth.backends :as backends]
            [ring.util.response :as response]))

(use 'api.users.users
     'ring.middleware.session
     'api.utils)

(require '[ring.middleware.cors :refer [wrap-cors]])

(defn token-validation [request data callback] (if (authenticate? request)
                                                 (callback data)
                                                 (unauthorized "Invalid Credentials!!")))

(defn auth-validation
  [request token]
  (let [token (keyword token)]
    token))

(def backend (backends/token {:authfn auth-validation}))

(defroutes app-routes
  (GET "/" [] "Server listenning...")
           (POST "/users" request (post-user request))
           (POST "/users/activate" request (activate-user request))
           (GET "/users/:username" request (token-validation request (:username (:params request)) get-user))
           (PUT "/users/:username"  request (token-validation request request put-user))
           (DELETE "/users" request (token-validation request request delete-user))
           (POST "/login" request (login request))
           (route/not-found "Not Found"))


(def app (-> (wrap-cors app-routes #".*")
             (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
             (middleware/wrap-json-response)
             (wrap-authentication backend)))