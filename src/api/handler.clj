(ns api.handler
  (:require [compojure.core :refer :all]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [buddy.auth.backends :as backends]
            [ring.util.response :as response]))

(use 'api.users.users
     'api.rooms.rooms
     'ring.middleware.session
     'api.utils)

(require '[ring.middleware.cors :refer [wrap-cors]])

(defn token-validation [request data callback] (if (authenticate? request)
                                                 (callback data)
                                                 (unauthorized "Invalid Credentials.")))

(def backend (backends/jws {:secret secret}))

(defroutes app-routes
  (GET "/" [] "Server listenning...")
           (POST "/login" request (login request))
           (POST "/users" request (post-user request))
           (POST "/users/activate" request (activate-user request))
           (POST "/users/add-friend" request (token-validation request {:identity (:identity request) :username (:body request)} add-friend))
           (POST "/users/remove-friend" request (token-validation request {:identity (:identity request) :username (:body request)} remove-friend))
           (GET "/users/:username" request (token-validation request (:username (:params request)) get-user))
           (GET "/users" request (token-validation request nil get-users))
           (PUT "/users/:username"  request (token-validation request request put-user))
           (DELETE "/users" request (token-validation request request delete-user))

           (POST "/rooms" request (token-validation request request post-room))
           (GET "/rooms/:title" request (token-validation request (:title (:params request)) get-room))
           (route/not-found "Not Found"))


(def app (-> (wrap-cors app-routes #".*")
             (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
             (middleware/wrap-json-response)
             (wrap-authentication backend)))