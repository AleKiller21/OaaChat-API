(ns api.users.users
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]))
(use 'api.utils)
(use 'api.users.db)
(use 'api.users.validations)

(def user_mail "oaachat@gmail.com")
(def pass "admin2102")
(def email-host "smtp.gmail.com")
(def secret "admin123")
(def email-conn {:host email-host :user user_mail :pass pass :ssl true})

; TODO: Email to user with activation code!
(defn new-user [body] (success (create-user body)))

(defn send-activation-code [{email :email hash :hash}]
  (let [message {:from user_mail
                 :to email
                 :subject "Activation code for the user you just created."
                 :body hash}]
    (println (send-email email-conn message))))

(defn post-user [{body :body}]
  (let [val-result (mand (val-email body)
                         (val-username body)
                         (val-password body)
                         (val-names body)
                         (val-birthday body)
                         (val-gender body))]
    (if (true? val-result)
      (do (let [data (merge body {:hash (hashers/derive (:email body))
                                  :password (hashers/derive (:password body))})]
            (let [response (new-user data)]
              (send-activation-code data)
              response)))
      val-result)))

(defn get-users [req]
  (let [users (find-users {:active true})]
    (get-dissoc users :_id :password :hash :active)))

(defn get-user [username]
  (let [user (find-user { :username username })]
    (if (nil? user)
      (not-found {:message "User not found."})
      (success (dissoc user :_id :password :hash :active)))))

;  TODO: Hay que validar que tenga un token para autorizar el update
(defn put-user [{{username :username} :params body :body}]
  (let [user (dissoc (find-user { :username username }) :age)
        new-vals (select-keys body [:username :firstname :lastname :birthday :gender :avatar])]
    (if (nil? user)
      (not-found {:message "User not found."})
      (let [val-result (mand (val-username-update new-vals (:username user))
                             (val-names new-vals)
                             (val-birthday new-vals)
                             (val-gender new-vals)
                             (val-avatar new-vals))]
        (if (true? val-result)
          (success (dissoc (update-user (:_id user) (merge user new-vals)) :_id :password :hash))
          val-result)))))

(defn delete-user [{body :body}]
  (let [email (:email body)
        password (:password body)
        user (and (not= nil email) (not= nil password) (find-user body))]
    (if (or (false? user) (nil? user))
      (unauthorized {:message "You are not authorized to do that."})
      (do
        (when (true? (:active user))
          (update-user (:_id user) (assoc user :active false)))
        (success (dissoc (assoc user :active false) :_id :hash))))))

(defn activate-user [{body :body}]
  (let [hash (:hash body)
        user (and (not= nil hash) (find-user (assoc body :active false)))]
    (if (or (nil? user) (false? user))
      (not-found {:message "Invalid hash."})
      (success (dissoc (update-user (:_id user) (assoc user :active true)) :_id :password :hash)))))

(defn login [{body :body}]
  (let [email (:email body)
        password (:password body)
        user (and (not= nil email) (not= nil password) (find-user {:email email :active true}))]
    (if (or (nil? user) (false? user))
      (not-found {:message "No active user exists with that email."})
      (if (= (hashers/check password (:password user)) true)
        (success {:hash (jwt/sign {:username (:username user)} secret)})
        (unauthorized {:message "Incorrect email or password."})))))

(defn add-friend [{identity :identity body :username}]
  (let [user_origin (find-user identity)
        user_destiny (find-user body)]
    (if (or (nil? user_destiny) (= (:active user_destiny) false))
      (not-found {:message "The user you want to add doesn't exist."})
      (do
        (if (not= nil (some (partial = (:username user_destiny)) (:friends user_origin)))
          (forbidden {:message "He is already in your list of friends."})
          (let [sender (merge user_origin {:friends (conj (:friends user_origin) (:username body))})
                receiver (merge user_destiny {:friends (conj (:friends user_destiny) (:username user_origin))})]
            (update-user (:_id user_origin) sender)
            (update-user (:_id user_destiny) receiver)
            (send-email email-conn {:from user_mail :to (:email user_destiny) :subject (str "You are now friends with " (:username sender))
                                    :body (str (:username user_origin) " has added you to his friends.")})
            (success (dissoc sender :_id))))))))

(defn remove-friend [{identity :identity body :username}]
  (let [user_origin (find-user identity)
        user_destiny (find-user body)]
    (if (or (nil? user_destiny) (= (:active user_destiny) false))
      (not-found {:message "The user you want to remove doesn't exist."})
      (do
        (if (nil? (some (partial = (:username user_destiny)) (:friends user_origin)))
          (forbidden {:message (str (:username user_destiny) " is not in your friends list.")})
          (let [sender (merge user_origin {:friends (remove #{(:username body)} (:friends user_origin))})
                receiver (merge user_destiny {:friends (remove #{(:username user_origin)} (:friends user_destiny))})]
            (update-user (:_id user_origin) sender)
            (update-user (:_id user_destiny) receiver)
            (success (dissoc sender :_id))))))))