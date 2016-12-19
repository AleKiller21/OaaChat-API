(ns api.rooms.rooms
  (:require [api.users.db :as users]
            [monger.operators :as ops]
            [api.users.db :refer [find-user update-user]]))
(use 'api.utils)
(use 'api.rooms.db)
(use 'api.rooms.validations)

(defn post-room [{identity :identity body :body}]
  (let [data (mand (val-room-title body)
                   (val-room-members body)
                   (val-room-visibility body))]
    (if (true? data)
      (do
        (users/add-users-room (:title body) (conj (:members body) (:username identity)))
        (success (create-room (merge body {:admin (:username identity)} {:members  (conj (:members body) (:username identity))}))))
      data)))

(defn add-users [{identity :identity members :members title :title}]
  (let [room (find-room {:title title})
        valid (mand (room-exist? room)
                    (users-exist? members))]
    (if (map? valid)
      valid
      (do
        (loop [[head & tail] members]
          (if (true? (member-exists? room [head]))
            (bad-request {:message (str head " is already in the room.")})
            (if (not= nil tail)
              (recur tail)
              (let [new-room (merge room {:members (apply conj (:members room) members)})]
                (update-room (:_id room) new-room)
                (users/add-users-room title members)
                (success (dissoc new-room :_id))))))))))

(defn select-new-admin [current-members remove-members]
  (loop [[head & tail] current-members]
    (if (nil? (some (partial = head) remove-members))
      head
      (if (not= nil tail)
        (recur tail)
        "none"))))

(defn remove-users [{identity :identity members :members title :title}]
  (let [room (find-room {:title title})
        valid (mand (room-exist? room)
                    (users-exist? members)
                    (member-exists? room members))]
    (if (map? valid)
      valid
      (loop [[head & tail] members room room]
        (if (not= nil head)
          (if (= head (:admin room))
            (recur tail (merge room {:members (remove #{head} (:members room)) :admin (select-new-admin (:members room) (conj tail head))}))
            (recur tail (merge room {:members (remove #{head} (:members room))})))
          (do
            (if (= (:admin room) "none")
              (delete-room (:_id room))
              (update-room (:_id room) room))
            (users/remove-users-room title members)
            (success (dissoc room :_id))))))))


(defn get-rooms
  ([req query]
   (let [rooms (find-rooms query)]
     (get-dissoc rooms :_id)))
  ([req]
   (let [rooms (find-rooms)]
     (success (get-dissoc rooms :_id)))))

(defn get-room [title]
  (let [room (find-room {:title title})]
    (if (nil? room)
      (not-found {:message "Room not found."})
      (success (dissoc room :_id)))))

(defn updtae-room-title-users [members old-title new-title]
  (doseq [member members]
    (let [user (find-user {:username member})
          rooms (:rooms user)]
      (if (nil? new-title)
        (update-user (:_id user) (merge user {:rooms (remove #{old-title} rooms)}))
        (update-user (:_id user) (merge user {:rooms (conj (remove #{old-title} rooms) new-title)}))))))

(defn put-room [{identity :identity body :body title :title}]
  (let [room (find-room {:title title})]
    (if (nil? room)
      (bad-request {:message (str title " room doesn't exist.")})
      (let [valid (mand (admin? identity room)
                        (val-room-visibility body)
                        (users-exist? [(:admin body)])
                        (member-exists? room [(:admin body)]))]
        (if (map? valid)
          valid
          (do
            (update-room (:_id room) (merge body {:messages (:messages room) :members (:members room)}))
            (updtae-room-title-users (:members room) title (:title body))
            (success (merge body {:messages (:messages room) :members (:members room)}))))))))

(defn remove-room [{identity :identity title :title}]
  (let [room (find-room {:title title})
        valid (mand (room-exist? room)
                    (admin? identity room))]
    (if (map? valid)
      valid
      (do
        (updtae-room-title-users (:members room) title nil)
        (delete-room (:_id room))
        (success (str title " room has been deleted."))))))

(defn remove-admin [room]
  (loop [[head & tail] (:members room)]
    (if (not= head (:admin room))
      (update-room (:_id room) (merge room {:admin head}))
      (if (not= nil tail)
        (recur tail)
        (do
          (updtae-room-title-users (:members room) (:title room) nil)
          (delete-room (:_id room))
          (success (str (:title room) " room has been deleted.")))))))

;(defn remove-admin [title]
;  (let [room (find-room {:title title})
;        valid (room-exist? room)]
;    (if (map? valid)
;      valid
;      (loop [[head & tail] (:members room)]
;        (if (not= head (:admin room))
;          (update-room (:_id room) (merge room {:admin head}))
;          (if (not= nil tail)
;            (recur tail)
;            (do
;              (updtae-room-title-users (:members room) title nil)
;              (delete-room (:_id room))
;              (success (str title " room has been deleted.")))))))))