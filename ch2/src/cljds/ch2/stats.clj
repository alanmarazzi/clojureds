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

(defn confidence-interval
  [p xs]
  (let [x-bar (s/mean xs)
        se (standard-error xs)
        z-crit (s/quantile-normal (- 1 (/ (- 1 p) 2)))]
    [(- x-bar (* se z-crit))
     (+ x-bar (* se z-crit))]))

(defn pooled-standard-deviation
  [a b]
  (i/sqrt (+ (i/sq (standard-deviation a))
             (i/sq (standard-deviation b)))))

(defn pooled-standard-error
  [a b]
  (i/sqrt (+ (/ (i/sq (standard-deviation a)) (count a))
             (/ (i/sq (standard-deviation b)) (count b)))))

(defn pooled-standard-error
  [a b]
  (i/sqrt (+ (i/sq (standard-error a))
             (i/sq (standard-error b)))))

(defn z-stat
  [a b]
  (-> (- (mean a)
         (mean b))
      (/ (pooled-standard-error a b))))

(defn z-test
  [a b]
  (s/cdf-normal (z-stat a b)))

(def t-stat z-stat)

(defn t-test
  [a b]
  (let [df (+ (count a) (count b) -2)]
    (- 1 (s/cdf-t (i/abs (t-stat a b)) :df df))))

(defn t-test-2-tails
  [a b]
  (let [df (+ (count a) (count b) -2)]
    (s/cdf-normal (- 1 (/ (- 0 1.64) 2)))))

(defn sst
  [groups]
  (->> (apply concat groups)
       (s/sum-of-square-devs-from-mean)))

(defn ssw
  [groups]
  (->> (map s/sum-of-square-devs-from-mean groups)
       (reduce +)))

(defn ssb
  [groups]
  (- (sst groups)
     (ssw groups)))

(defn f-stat
  [groups df1 df2]
  (let [msb (/ (ssb groups) df1)
        msw (/ (ssw groups) df2)]
    (/ msb msw)))

(defn f-test
  [groups]
  (let [n (count (apply concat groups))
        m (count groups)
        df1 (- m 1)
        df2 (- n m)
        f-stat (f-stat groups df1 df2)]
    (s/cdf-f f-stat :df1 df1 :df2 df2 :lower-tail? false)))
