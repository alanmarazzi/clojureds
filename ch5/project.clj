(defproject cljds/ch5 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter/incanter "1.5.7"]
                 [tesser.core "1.0.2"]
                 [tesser.math "1.0.2"]
                 [tesser.hadoop "1.0.2"]
                 [org.clojure/tools.cli "0.3.1"]
                 [transduce "0.1.1"]
                 [org.apache.avro/avro "1.7.5"]
                 [org.apache.avro/avro-mapred "1.7.5"
                  :classifier "hadoop2"]
                 [com.damballa/parkour "0.6.3"]
                 [iota "1.1.3"]]
  :resource-paths ["data"]
  :plugins [[lein-gorilla "0.4.0"]]
  :aot [cljds.ch5.core]
  :main cljds.ch5.core
  :repl-options {:init-ns cljds.ch5.examples}
  :profiles {:dev {:dependencies [[org.clojure/tools.cli "0.3.1"]]}})

