(defproject cljds/ch3 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter/incanter "1.5.7"]
                 [clj-time "0.14.2"]]
  :resource-paths ["data"]
  :aot [cljds.ch3.core]
  :main cljds.ch3.core
  :repl-options {:init-ns cljds.ch3.examples}
  :profiles {:dev {:dependencies [[org.clojure/tools.cli "0.3.1"]]}})
