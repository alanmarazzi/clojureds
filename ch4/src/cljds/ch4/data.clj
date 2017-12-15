(ns cljds.ch4.data
  (:require [incanter.core :as i]
            [incanter.io :as iio] 
            [clojure.java.io :as io]
            [clj-ml.io :as cio]
            [incanter.excel :as xls]
            [clj-ml.data :as mld]
            [clj-ml.filters :as mlf]))

(defn load-data
  [file]
  (-> (io/resource file)
      (str)
      (iio/read-dataset :delim \tab :header true)))

(defn frequency-table
  [sum-column group-columns dataset]
  (->> (i/$ group-columns dataset)
       (i/add-column sum-column (repeat 1))
       (i/$rollup :sum sum-column group-columns)))

(defn frequency-map
  [sum-column group-columns dataset]
  (let [f (fn [freq-map row]
            (let [groups (map row group-columns)]
              (->> (get row sum-column)
                   (assoc-in freq-map groups))))]
    (->> (frequency-table sum-column group-columns dataset)
         (:rows)
         (reduce f {}))))

(defn fatalities-by-sex
  [dataset]
  (let [totals (frequency-map :count [:sex] dataset)
        groups (frequency-map :count [:sex :survived] dataset)]
    {:male (/ (get-in groups ["male" "n"])
              (get totals "male"))
     :female (/ (get-in groups ["female" "n"])
                (get totals "female"))}))

(defn expected-frequencies
  [data]
  (let [as (vals (frequency-map :count [:survived] data))
        bs (vals (frequency-map :count [:pclass] data))
        total (-> data :rows count)]
    (for [a as
          b bs]
      (* a (/ b total)))))

(defn observed-frequencies [data]
  (let [as (frequency-map :count [:survived] data)
        bs (frequency-map :count [:pclass] data)
        actual (frequency-map :count [:survived :pclass] data)]
    (for [a (keys as)
          b (keys bs)]
      (get-in actual [a b]))))
