(defproject cljds/ch2 "0.1.0"
  :description "Chapter 2 code"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [incanter/incanter "1.5.7"]
                 [medley "1.0.0"]
                 [clj-time "0.14.2"]
                 [b1 "0.3.3"]
                 [reagent "0.7.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [incanter-gorilla "0.1.0"]]
  :resource-paths ["data"]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-gorilla "0.4.0"]]
  :aot [cljds.ch2.core]
  :main cljds.ch2.core
  :repl-options {:init-ns cljds.ch2.examples}
  :profiles {:dev {:dependencies [[org.clojure/tools.cli "0.3.1"]]}}

  :cljsbuild
  {:builds
   {:client {:source-paths ["src"]
             :compiler
             {:preamble ["reagent/react.js"]
              :output-dir "target/app"
              :output-to "target/app.js"
              :pretty-print true}}}})
