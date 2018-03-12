(defproject cljds/ch5 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter/incanter "1.5.7"]]
  :resource-paths ["data"]
  :plugins [[lein-gorilla "0.4.0"]]
;  :aot [cljds.ch5.core]
  :main cljds.ch5.core
  :repl-options {:init-ns cljds.ch5.examples}
  :profiles {:dev {:dependencies [[org.clojure/tools.cli "0.3.1"]]}})

