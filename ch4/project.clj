(defproject cljds/ch4 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter/incanter "1.5.7"]
                 [cc.artifice/clj-ml "0.8.1"]]
  :resource-paths ["resources" "data"]
  :aot [cljds.ch4.core]
  :main cljds.ch4.core
  :repl-options {:init-ns cljds.ch4.examples}
  :profiles {:dev {:dependencies [[org.clojure/tools.cli "0.3.1"]]}})
