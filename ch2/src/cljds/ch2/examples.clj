(ns cljds.ch2.examples
  (:require [cljds.ch2.data :refer :all]
            [cljds.ch2.stats :refer :all]
            [clj-time.format :as f]
            [clj-time.predicates :as p]
            [medley.core :refer [map-vals]]
            [clj-time.core :as t]
            [incanter.charts :as c]
            [incanter.core :as i]
            [incanter.io :as iio]
            [incanter.stats :as s]
            [incanter.svg :as svg]))

(defn ex-2-1
  []
  (-> (load-data "dwell-times.tsv")
      (i/view)))

(defn ex-2-2
  []
  (-> (i/$ :dwell-time (load-data "dwell-times.tsv"))
      (c/histogram :x-label "Dwell time (s)"
                   :nbins 50)
      (i/view)))

; log axis
(defn ex-2-3
  []
  (-> (i/$ :dwell-time (load-data "dwell-times.tsv"))
      (c/histogram :x-label "Dwell time (s)"
                   :nbins 20)
      (c/set-axis :y (c/log-axis :label "Log Frequency"))
      (i/view)))

(defn ex-2-4
  []
  (let [dwell-times (->> (load-data "dwell-times.tsv")
                         (i/$ :dwell-time))]
    (println "Mean:   " (s/mean dwell-times))
    (println "Median: " (s/median dwell-times))
    (println "SD:     " (s/sd dwell-times))))

(defn ex-2-5
  []
  (let [means (->> (load-data "dwell-times.tsv")
                   (daily-mean-dwell-times)
                   (i/$ :dwell-time))]
    (println "Mean:   " (s/mean means))
    (println "Median: " (s/median means))
    (println "SD:     " (s/sd means))))

(defn ex-2-6
  []
  (let [means (->> (load-data "dwell-times.tsv")
                   (daily-mean-dwell-times)
                   (i/$ :dwell-time))]
    (-> (c/histogram means
                     :x-label "Daily mean"
                     :nbins 20)
        (i/view))))

(defn ex-2-7
  []
  (let [means (->> (load-data "dwell-times.tsv")
                   (daily-mean-dwell-times)
                   (i/$ :dwell-time))
        mean (s/mean means)
        sd (s/sd means)
        pdf (fn [x] (s/pdf-normal x :mean mean :sd sd))]
    (-> (c/histogram means
                     :x-label "Daily mean"
                     :nbins 20
                     :density true)
        (c/add-function pdf 80 100)
        (i/view))))

; Standard error
(defn ex-2-8
  []
  (let [may-1 (f/parse-local-date "2015-05-01")]
    (->> (load-data "dwell-times.tsv")
         (with-parsed-date)
         (filtered-times {:date {:$eq may-1}})
         (standard-error))))

; Confidence intervals
(defn ex-2-9
  []
  (let [may-1 (f/parse-local-date "2015-05-01")]
    (->> (load-data "dwell-times.tsv")
         (with-parsed-date)
         (filtered-times {:date {:$eq may-1}})
         (confidence-interval 0.95))))

; Sample comparison
(defn ex-2-10
  []
  (let [times (->> (load-data "campaign-sample.tsv")
                   (i/$ :dwell-time))]
    (println "n:      " (count times))
    (println "Mean:   " (s/mean times))
    (println "Median: " (s/median times))
    (println "SD:     " (s/sd times))
    (println "SE:     " (standard-error times))))

(defn ex-2-11
  []
  (->> (load-data "campaign-sample.tsv")
       (i/$ :dwell-time)
       (confidence-interval 0.95)))

(defn ex-2-12
  []
  (let [means (->> (load-data "dwell-times.tsv")
                   (with-parsed-date)
                   (mean-dwell-times-by-date)
                   (i/$ :dwell-time))]
    (-> (c/histogram means
                     :x-label "Daily mean"
                     :nbins 20)
        (i/view))))

(defn ex-2-13
  []
  (let [weekend-times (->> (load-data "dwell-times.tsv")
                           (with-parsed-date)
                           (i/$where {:date {:$fn p/weekend?}})
                           (i/$ :dwell-time))]
    (println "n:      " (count weekend-times))
    (println "Mean:   " (s/mean weekend-times))
    (println "Median: " (s/median weekend-times))
    (println "SD:     " (s/sd weekend-times))
    (println "SE:     " (standard-error weekend-times))))

(defn ex-2-14
  []
  (let [data (->> (load-data "new-site.tsv")
                  (:rows)
                  (group-by :site)
                  (map-vals (partial map :dwell-time)))
        a (get data 0)
        b (get data 1)]
    (println "a n: " (count a))
    (println "b n: " (count b))
    (println "z-stat: " (z-stat a b))
    (println "p-value: " (z-test a b))))

(defn ex-2-15
  []
  (let [data (->> (load-data "new-site.tsv")
                  (:rows)
                  (group-by :site)
                  (map-vals (partial map :dwell-time)))
        a (get data 0)
        b (get data 1)]
    (t-stat a b)))

(defn ex-2-16
  "One tail t-test"
  []
  (let [data (->> (load-data "new-site.tsv")
                  (:rows)
                  (group-by :site)
                  (map-vals (partial map :dwell-time)))
        a (get data 0)
        b (get data 1)]
    (t-test a b)))

