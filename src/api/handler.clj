(ns api.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]))

(use 'api.users.users)

(defroutes app-routes
  (GET "/" [] "Server listenning...")
           (POST "/users" request (post-user request))
  (route/not-found "Not Found"))

(def app (-> (handler/site app-routes)
             (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
             (middleware/wrap-json-response)))