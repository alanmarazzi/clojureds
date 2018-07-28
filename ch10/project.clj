(defproject ch10 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [quil "2.7.1"]
                 [incanter "1.5.7"]]
  :resource-paths ["data"]
  :jvm-opts ["-Xmx4G"]
  :plugins [[lein-gorilla "0.4.1-SNAPSHOT"]])
