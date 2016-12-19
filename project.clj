(defproject api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-devel "1.1.8"]
                 [com.novemberain/monger "3.1.0"]
                 [clj-time "0.12.2"]
                 [com.draines/postal "2.0.2"]
                 [buddy/buddy-hashers "1.0.0"]
                 [buddy/buddy-auth "1.3.0"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [http-kit "2.2.0"]
                 [cheshire "5.6.3"]]
  :main api.handler
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
