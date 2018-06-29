(defproject ch8 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [loom-gorilla "0.1.0"]
                 [aysylu/loom "1.0.1"]
                 [incanter "1.5.7"]
                 [t6/from-scala "0.3.0"]
                 [glittering "0.1.2"]
                 [gorillalabs/sparkling "1.2.2"]
                 [org.apache.spark/spark-core_2.11 "1.3.1"]
                 [org.apache.spark/spark-graphx_2.11 "1.3.1"]]
  :plugins [[dtolpin/lein-gorilla "0.4.1-SNAPSHOT"]]
  :main ^:skip-aot ch8.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["data" "dev-resources"]}})
