(defproject cljds/ch1 "0.1.0"
  :description "Personal work on `Clojure Data Science` book"
  :url "http://github.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter/incanter "1.5.7"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [me.raynes/fs "1.4.6"]
	         [incanter-gorilla "0.1.0"]]
  :resource-paths ["data"]
  :aot [cljds.ch1.core]
  :main cljds.ch1.core
  :plugins [[lein-gorilla "0.4.0"]]
  :repl-options {:init-ns cljds.ch1.examples}
  :profiles {:dev {:dependencies [[org.clojure/tools.cli "0.3.1"]]}})
  :jvm-opts ["-Xmx2G"]
