(ns api.users.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(defn exists? [query]
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"]
    (nil? (mc/find db coll query))))