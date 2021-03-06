(ns api.rooms.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all])
  (:import [org.bson.types ObjectId]))

(require '[clj-time.format :as f])

(use 'api.utils)

(def db-name "monger-test")
(def coll "rooms")

(defn create-room [data]
  (let [conn (mg/connect)
        db   (mg/get-db conn db-name)
        room (merge data {:messages [] :admin (:admin data)})]
    (mc/insert db coll (merge { :_id (ObjectId.) } room))
    room))

(defn find-rooms
  ([query]
   (let [conn (mg/connect)
         db (mg/get-db conn db-name)
         results (mc/find-maps db coll query)]
     results))
  ([]
   (let [conn (mg/connect)
         db (mg/get-db conn db-name)
         results (mc/find-maps db coll)]
     results)))

(defn find-room [query] (first (find-rooms query)))

(defn update-room [id new-room]
  (let [conn (mg/connect)
        db (mg/get-db conn db-name)]
    (mc/update-by-id db coll id new-room)
    new-room))

(defn delete-room [id]
  (let [conn (mg/connect)
        db (mg/get-db conn db-name)]
    (mc/remove-by-id db coll id)))

(defn exists? [query] (not (empty? (find-rooms query))))


