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

(defn get-user [username] (let [res (find-users { :username username })]
                            (if (empty? res)
                              {:status 404 :body "User not found."}
                              {:status 200 :body (dissoc (first res) :_id)})))