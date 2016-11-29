(ns api.users.validations)
(use 'api.utils)
(use 'api.users.db)
(require '[clj-time.core :as t])
(require '[clj-time.format :as f])

(defn val-email [{ email :email }]
  (if (nil? email)
   (bad-request  "You must provide an email.")
   (if (nil? (re-matches email-regex email))
     (bad-request "Invalid email.")
     (if (exists? { :email email })
       (bad-request "There is already an account with that email address.")
       true))))

(defn val-username [{username :username}]
  (if (nil? username)
    (bad-request "You  must provide a username.")
    (if (nil? (re-matches username-regex username))
      (bad-request "Username must be at least 4 characters long.")
      (if (exists? {:username username})
        (bad-request "There is already an account with that username.")
        true))))

(defn val-username-update [{username :username} original] (if (= username original) true (val-username {:username username})))

(defn val-avatar [{avatar :avatar}]
  (if (nil? avatar)
    (bad-request "You must provide an avatar.")
    true))

(defn val-password [{ password :password }]
  (if (nil? password)
    (bad-request "You must provide a password.")
    (do (if (nil? (re-matches password-regex password))
          (bad-request "Password must be a minimum of 8 characters long with at least 2 numbers and a capital letter.")
          true))))

(defn val-names [{ first :firstname last :lastname }]
  (if (or (nil? first) (empty? first))
    (bad-request "You must provide a first name.")
    (if (or (nil? last) (empty? last))
      (bad-request "You must provide a last name.")
      true)))

(defn val-birthday [{ birthday :birthday }]
  (if (nil? birthday)
    (bad-request "You must provide a birthday.")
    (let [now (t/today-at 12 00)
          parsed-date (f/parse (f/formatter "yyyy-MM-dd") birthday)]
      (if (or (t/equal? parsed-date now) (t/after? parsed-date now))
        (bad-request "That cannot possibly be your birthday!")
        true))))

(defn val-gender [{ gender :gender }]
  (if (nil? gender)
    (bad-request "You must provide your gender.")
    (if (or (= gender "Male") (= gender "Female"))
      true
      (bad-request "Gender MUST be binary."))))