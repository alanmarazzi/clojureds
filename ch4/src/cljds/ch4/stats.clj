(ns cljds.ch4.stats
  (:require [clojure.set :as set]
            [incanter.core :as i]
            [incanter.optimize :as o]
            [incanter.stats :as s]))

(defn relative-risk
  [p1 p2]
  (float (/ p1 p2)))

(defn odds-ratio
  [p1 p2]
  (float
   (/ (* p1 (- 1 p2))
      (* p2 (- 1 p1)))))

(defn standard-error-proportion
  [p n]
  (-> (- 1 p)
      (* p)
      (/ n)
      (i/sqrt)))

(defn se-large-proportion
  [p n N]
  (* (standard-error-proportion p n)
     (i/sqrt (/ (- N n)
                (- n 1)))))

(defn chisq-stat
  [observed expected]
  (let [f (fn [observed expected]
            (/ (i/sq (- observed expected)) expected))]
    (reduce + (map f observed expected))))

