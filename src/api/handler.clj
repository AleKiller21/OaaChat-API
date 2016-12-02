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

(def tokens {:2f904e245c1f5 :admin
             :45c1f5e3f05d0 :foouser})

(defn token-validation
  [request data callback]
  (if (:identity request)
    (callback data)
    (unauthorized "Invalid Credentials!!")))

(defn auth-validation
  [request token]
  (let [token (keyword token)]
    (get tokens token nil)))

(defn validate-token [request ])

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

(def app (-> (handler/site app-routes)
             (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
             (middleware/wrap-json-response)
             (wrap-authentication backend)))