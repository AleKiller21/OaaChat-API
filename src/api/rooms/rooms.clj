(ns api.rooms.rooms)
(use 'api.utils)
(use 'api.rooms.db)
(use 'api.rooms.validations)

(defn post-room [{identity :identity body :body}]
  (let [data (mand (val-room-title body)
                   (val-room-description body)
                   (val-room-members body)
                   (val-room-visibility body))]
    (if (true? data)
      (success (create-room (merge body {:admin (:username identity)} {:members  (conj (:members body) (:username identity))})))
      data)))

(defn get-room [title]
  (let [room (find-room {:title title})]
    (if (nil? room)
      (not-found {:message "Room not found."})
      (success (dissoc room :_id)))))