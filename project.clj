(defproject api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [com.novemberain/monger "3.1.0"]
                 [clj-time "0.12.2"]
                 [com.draines/postal "2.0.2"]
                 [buddy/buddy-hashers "1.0.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler api.handler/app
         :auto-reload? true
         :auto-refresh? true}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
