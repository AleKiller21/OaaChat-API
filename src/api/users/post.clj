(ns api.users.post)
(use 'api.users.db)

(def email-regex #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")

(defn val-email [request]
  (let [email (get-in request [:body :email])]
             (if (nil? email)
               {:status 400 :body "You must provide an email."}
               (do (if (nil? (re-matches email-regex email))
                     {:status 400 :body "Invalid email."}
                     true)))))

(defn val-unique-email-and-username [request]
  (println (exists? {:first_name "John"}))
  (or (exists? {:first_name "wupa"}) {:status 400 :body "An account with that email already exists."}))

(defn val-password [request]
  (let [email (get-in request [:body :email])]
    (if (nil? email)
      {:status 400 :body "You must provide an email."}
      (do (if (nil? (re-matches email-regex email))
            {:status 400 :body "Invalid email."}
            true)))))

(defn val-date [request]
  (let [email (get-in request [:body :email])]
    (if (nil? email)
      {:status 400 :body "You must provide an email."}
      (do (if (nil? (re-matches email-regex email))
            {:status 400 :body "Invalid email."}
            true)))))

(defn val-gender [request]
  (let [email (get-in request [:body :email])]
    (if (nil? email)
      {:status 400 :body "You must provide an email."}
      (do (if (nil? (re-matches email-regex email))
            {:status 400 :body "Invalid email."}
            "Great!")))))

(defn post-user [request] (and (val-email request)
                               (val-unique-email-and-username request)
                               (val-password request)
                               (val-date request)
                               (val-gender request)))