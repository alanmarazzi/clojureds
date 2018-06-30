(defproject ch8 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [aysylu/loom "0.5.0"]
                 [incanter "1.5.6"]
                 [t6/from-scala "0.2.1"]
                 [glittering "0.1.2"]
                 [gorillalabs/sparkling "1.2.2"]
                 [org.apache.spark/spark-core_2.11 "1.3.1"]
                 [org.apache.spark/spark-graphx_2.11 "1.3.1"]]
  :plugins [[dtolpin/lein-gorilla "0.4.1-SNAPSHOT"]]
  :main ^:skip-aot ch8.core
  :target-path "target/%s"
  :aot [cljds.ch8.glittering]
  :profiles {:dev {:resource-paths ["data" "dev-resources"]}})
