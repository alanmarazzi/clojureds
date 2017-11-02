(ns cljds.ch2.stats
  (:require [incanter.stats :as s]
            [incanter.core :as i]
            [incanter.charts :as c]
            [incanter.svg :as svg]))

(defn mean
  [xs]
  (/ (apply + xs)
     (count xs)))

(defn variance
  [xs]
  (let [m (mean xs)
        square-error (fn [x] (Math/pow (- x m) 2))]
    (mean (map square-error xs))))

(defn standard-deviation
  [xs]
  (Math/sqrt (variance xs)))

(defn standard-error
  [xs]
  (/ (standard-deviation xs)
     (Math/sqrt (count xs))))
