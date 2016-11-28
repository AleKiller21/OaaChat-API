(ns api.users.users)
(use 'api.utils)
(use 'api.users.db)
(use 'api.users.validations)

; TODO: Email to user with activation code!
(defn new-user [body] {:status 200 :body (create-user body)})

(defn post-user [{body :body}] (let [val-result (mand (val-email body)
                                                      (val-unique-email-and-username body)
                                                      (val-password body)
                                                      (val-names body)
                                                      (val-date body)
                                                      (val-gender body))]
                                 (if (true? val-result) (new-user body) val-result)))

(defn get-user [username] (let [user (find-user { :username username })]
                            (if (nil? user)
                              {:status 404 :body "User not found."}
                              {:status 200 :body (dissoc user :_id)})))

(defn delete-user [{body :body}] (let [email (:email body)
                                       password (:password body)
                                       user (and (not= nil email) (not= nil password) (find-user body))]
                                   (if (or (false? user) (nil? user))
                                     {:status 404 :body "No user found with those credentials."}
                                     (do
                                       (if (true? (:active user))
                                         (update-user (:_id user) (update user :active false)))
                                       {:status 200 :body (dissoc user :_id)}))))