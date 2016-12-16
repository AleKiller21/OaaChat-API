(ns api.users.db
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [org.bson.types ObjectId]))

(require '[clj-time.format :as f])

(use 'api.utils)

(def db-name "monger-test")
(def coll "users")

(defn find-users [query]
  (let [conn (mg/connect)
        db (mg/get-db conn db-name)
        results (mc/find-maps db coll query)]
    (loop [[res & remaining] results
           new-results []]
      (println res)
      (if (nil? res)
        new-results
        (recur remaining (conj new-results (merge res { :age (age (f/parse (f/formatter "yyyy-MM-dd") (:birthday res))) })))))))

(defn find-user [query] (first (find-users query)))

(defn exists? [query] (not (empty? (find-users query))))

(defn update-user [id new-user]
  (let [conn (mg/connect)
        db (mg/get-db conn db-name)]
    (mc/update-by-id db coll id new-user)
    (merge new-user { :age (age (f/parse (f/formatter "yyyy-MM-dd") (:birthday new-user))) })))

(defn create-user [data]
  (let [conn (mg/connect)
        db   (mg/get-db conn db-name)
        user (merge data { :avatar "http://localhost:3000/default-avatar.png" :friends [] :active false})]
    (mc/insert db coll (merge { :_id (ObjectId.) } user))
    (merge user { :age (age (f/parse (f/formatter "yyyy-MM-dd") (:birthday user))) })))
