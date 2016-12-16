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


