(ns api.users.users)
(use 'api.utils)
(use 'api.users.db)
(use 'api.users.validations)

; TODO: Email to user with activation code!
(defn new-user [body] (success (create-user body)))

(defn post-user [{body :body}] (let [val-result (mand (val-email body)
                                                      (val-username body)
                                                      (val-password body)
                                                      (val-names body)
                                                      (val-birthday body)
                                                      (val-gender body))]
                                 (if (true? val-result) (new-user body) val-result)))

(defn get-user [username] (let [user (find-user { :username username })]
                            (if (nil? user)
                              (not-found "User not found.")
                              (success (dissoc user :_id :password)))))

;  TODO: Hay que validar que tenga un token para autorizar el update
(defn put-user [{{username :username} :params body :body}] (let [user (dissoc (find-user { :username username }) :age)
                                                                 new-vals (select-keys body [:username :firstname :lastname :birthday :gender :avatar])]
                                                             (if (nil? user)
                                                               (not-found "User not found.")
                                                               (let [val-result (mand (val-username-update new-vals (:username user))
                                                                                      (val-names new-vals)
                                                                                       (val-birthday new-vals)
                                                                                       (val-gender new-vals)
                                                                                      (val-avatar new-vals))]
                                                                 (if (true? val-result)
                                                                   (success (dissoc (update-user (:_id user) (merge user new-vals)) :_id :password))
                                                                   val-result)))))

(defn delete-user [{body :body}] (let [email (:email body)
                                       password (:password body)
                                       user (and (not= nil email) (not= nil password) (find-user body))]
                                   (if (or (false? user) (nil? user))
                                     (unauthorized "You are not authorized to do that.")
                                     (do
                                       (when (true? (:active user))
                                         (update-user (:_id user) (assoc user :active false)))
                                       (success (dissoc (assoc user :active false) :_id))))))
