(defproject ch7 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.apache.mahout/mahout-core "0.9" :exclusions [com.google.guava/guava]]
                 [org.apache.mahout/mahout-examples "0.9" :exclusions [com.google.guava/guava]]
                 [gorillalabs/sparkling "2.1.3"]
                 [me.raynes/fs "1.4.6"]
                 [medley "1.0.0"]
                 [incanter "1.5.7"]
                 [com.google.guava/guava "16.0"]
                 [iota "1.1.3"]]
  :plugins [[lein-jupyter "0.1.16"]]
  :profiles {:dev
             {:dependencies [[org.clojure/tools.cli "0.3.1"]]
              :repl-options {:init-ns ch7.core}
              ;:resource-paths ["data/ml-100k"]
              }
             :provided
             {:dependencies
              [[org.apache.spark/spark-mllib_2.10 "1.1.0" :exclusions [com.google.guava/guava]]
               [org.apache.spark/spark-core_2.10 "1.1.0" :exclusions [com.google.guava/guava com.thoughtworks.paranamer/paranamer]]]}}
  :main ch7.core
  :jvm-opts ["-Xmx4g"])
