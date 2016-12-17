(ns api.rooms.rooms
  (:require [api.users.db :as users]))
(use 'api.utils)
(use 'api.rooms.db)
(use 'api.rooms.validations)

(defn post-room [{identity :identity body :body}]
  (let [data (mand (val-room-title body)
                   (val-room-description body)
                   (val-room-members body)
                   (val-room-visibility body))]
    (if (true? data)
      (do
        (users/update-users-room (:title body) (conj (:members body) (:username identity)))
        (success (create-room (merge body {:admin (:username identity)} {:members  (conj (:members body) (:username identity))}))))
      data)))

(defn add-users [{identity :identity members :members title :title}]
  (let [room (find-room {:title title})
        valid (users-exist? members)]
    (if (nil? room)
      (not-found {:message (str title " room doesn't exist.")})
      (if (map? valid)
        (bad-request valid)
        (if (not (admin? (:username identity) room))
          (forbidden {:message "Only the admin can add new members to this group."})
          (let [valid-members (member-exists? room members)]
            (if (map? valid-members)
              (bad-request valid-members)
              (let [new-room (merge room {:members (apply conj (:members room) members)})]
                (update-room (:_id room) new-room)
                (users/update-users-room title members)
                (success (dissoc new-room :_id))))))))))

(defn get-rooms [req]
  (let [rooms (find-rooms)]
    (get-dissoc rooms :_id)))

(defn get-room [title]
  (let [room (find-room {:title title})]
    (if (nil? room)
      (not-found {:message "Room not found."})
      (success (dissoc room :_id)))))
