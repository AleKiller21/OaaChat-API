(ns api.handler
  (:require [compojure.core :refer :all]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [buddy.auth.backends :as backends]
            [ring.util.response :as response]
            [api.users.users :as users]
            [api.rooms.rooms :as rooms]))

(use 'ring.middleware.session
     'api.utils)

(require '[ring.middleware.cors :refer [wrap-cors]])

(defn token-validation [request data callback] (if (authenticate? request)
                                                 (callback data)
                                                 (unauthorized "Invalid Credentials.")))

(def backend (backends/jws {:secret users/secret}))

(defroutes app-routes
  (GET "/" [] "Server listenning...")
           (POST "/login" request (users/login request))
           (POST "/users" request (users/post-user request))
           (POST "/users/activate" request (users/activate-user request))
           (POST "/users/add-friend" request (token-validation request {:identity (:identity request) :username (:body request)} users/add-friend))
           (POST "/users/remove-friend" request (token-validation request {:identity (:identity request) :username (:body request)} users/remove-friend))
           (GET "/users" request (token-validation request nil users/get-users))
           (GET "/users/rooms" request (token-validation request (:username (:identity request)) users/get-rooms))
           (GET "/users/:username" request (token-validation request (:username (:params request)) users/get-user))
           (GET "/me" request (token-validation request (:username (:identity request)) users/me))
           (PUT "/users/:username"  request (token-validation request request users/put-user))
           (DELETE "/users" request (token-validation request request users/delete-user))

           (POST "/rooms" request (token-validation request request rooms/post-room))
           (POST "/rooms/add-users" request (token-validation request {:identity (:identity request) :members (:members (:body request))
                                                                       :title (:title (:body request))} rooms/add-users))
           (POST "/rooms/remove-users" request (token-validation request {:identity (:identity request) :members (:members (:body request))
                                                                         :title (:title (:body request))} rooms/remove-users))
           (GET "/rooms" request (token-validation request request rooms/get-rooms))
           (GET "/rooms/:title" request (token-validation request (:title (:params request)) rooms/get-room))
           (route/not-found "Not Found"))


(def app (-> (wrap-cors app-routes #".*")
             (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
             (middleware/wrap-json-response)
             (wrap-authentication backend)))