(ns api.rooms.validations
  (:require [api.rooms.db :as rooms]))
(use 'api.utils)
(use 'api.users.db)
;;(use 'api.rooms.db)

(defn members-exist? [members]
  (loop [[head & tail] members val true]
    (if (nil? (find-user {:username head :active true}))
      {:message (str head " doesn't exist.")}
      (if (nil? tail)
        val
        (recur tail true)))))

(defn val-room-title [{ title :title }]
  (if (empty? title)
    (bad-request {:message "The title of the room can't be empty."})
    (if (rooms/exists? {:title title})
      (bad-request {:message "A room with that title already exists."})
      true)))

(defn val-room-description [{ description :description }]
  (if (empty? description)
    (bad-request {:message "The description of the room can't be empty."})
    true))

(defn val-room-members [{ members :members }]
  (if (empty? members)
    (bad-request {:message "The room must have at least one member."})
    (let [valid-member (members-exist? members)]
      (if (map? valid-member)
        (bad-request valid-member)
        true))))

(defn val-room-visibility [{ visibility :visibility }]
  (if (or (empty? visibility) (and (not= visibility "public") (not= visibility "private")))
    (bad-request {:message "The room must be either public or private."})
    true))