(ns api.users.validations)
(use 'api.utils)
(use 'api.users.db)
(require '[clj-time.core :as t])
(require '[clj-time.format :as f])

(defn val-email [{ email :email }]
  (if (nil? email)
    {:status 400 :body "You must provide an email."}
    (do (if (nil? (re-matches email-regex email))
          {:status 400 :body "Invalid email."}
          true))))

(defn val-unique-email-and-username [{ email :email username :username }]
  (if (nil? email)
    {:status 400 :body "You must provide an email."}
    (if (nil? username)
      {:status 400 :body "You must provide a username."}
      (if (exists? { :email email })
        {:status 400 :body "There is already an account with that email address."}
        (if (exists? { :username username })
          {:status 400 :body "There is already an account with that username."}
          (if (nil? (re-matches username-regex username))
            {:status 400 :body "Username must be at least 4 characters long."}
            true))))))

(defn val-password [{ password :password }]
  (if (nil? password)
    {:status 400 :body "You must provide a password."}
    (do (if (nil? (re-matches password-regex password))
          {:status 400 :body "Password must be a minimum of 8 characters long with at least 2 numbers and a capital letter."}
          true))))

(defn val-names [{ first :firstname last :lastname }]
  (if (or (nil? first) (empty? first))
    {:status 400 :body "You must provide a first name."}
    (if (or (nil? last) (empty? last))
      {:status 400 :body "You must provide a last name."}
      true)))

(defn val-date [{ birthday :birthday }]
  (if (nil? birthday)
    {:status 400 :body "You must provide a birthday."}
    (let [now (t/today-at 12 00)
          parsed-date (f/parse (f/formatter "yyyy-MM-dd") birthday)]
      (if (or (t/equal? parsed-date now) (t/after? parsed-date now))
        {:status 400 :body "That cannot possibly be your birthday!"}
        true))))

(defn val-gender [{ gender :gender }]
  (if (nil? gender)
    {:status 400 :body "You must provide your gender."}
    (if (or (= gender "Male") (= gender "Female"))
      true
      {:status 400 :body "Gender MUST be binary."})))