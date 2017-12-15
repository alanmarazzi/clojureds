(ns cljds.ch3.data
  (:require [clojure.java.io :as io]
            [incanter.core :as i]
            [incanter.excel :as xls]))

(defn athlete-data []
  (-> (io/resource "all-london-2012-athletes.xlsx")
      (str)
      (xls/read-xls)))

(defn swimmer-data []
  (->> (athlete-data)
       (i/$where {"Height, cm" {:$ne nil} "Weight" {:$ne nil}
                  "Sport" {:$eq "Swimming"}})))

(defn feature-matrix
  [col-names dataset]
  (-> (i/$ col-names dataset)
      (i/to-matrix)))
