(defproject ch6 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojure-opennlp "0.4.0"]
                 [org.apache.mahout/mahout-core "0.9" :exclusions [org.apache.hadoop/hadoop-core]]
                 [org.apache.mahout/mahout-integration "0.9"]
                 [stemmers "0.2.2"]
                 [org.apache.lucene/lucene-benchmark "4.10.3"]
                 [incanter "1.5.7"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [cc.mallet/mallet "2.0.7"]
                 [me.raynes/fs "1.4.6"]
                 [com.damballa/parkour "0.6.3"]
                 [com.damballa/abracad "0.4.14-alpha2"]
                 [org.apache.hadoop/hadoop-client "2.7.0"]
                 [org.apache.hadoop/hadoop-common "2.7.0"]
                 [org.apache.hadoop/hadoop-hdfs "2.7.0"]
                 [org.apache.avro/avro-mapred "1.7.5" :classifier "hadoop2"]
                 [org.slf4j/slf4j-api "1.6.1"]
                 [org.slf4j/slf4j-log4j12 "1.6.1"]
                 [log4j "1.2.17"]]
  :plugins [[lein-gorilla "0.4.0"]]
  :profiles
  {:dev
   {:dependencies [[org.clojure/tools.cli "0.3.1"]]
    ;:repl-opts {:init-ns cljds.ch6.examples}
    :resource-paths ["dev-resources"
                     "data/reuters-text"]}})
