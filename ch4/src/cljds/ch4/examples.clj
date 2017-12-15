(ns cljds.ch4.examples
  (:require ;[cljds.ch4.bayes :refer :all]
            [cljds.ch4.data :refer :all]
            ;[cljds.ch4.decision-tree :refer :all]
            ;[cljds.ch4.logistic :refer :all]
            [cljds.ch4.stats :refer :all]
            [clj-ml.classifiers :as cl]
            [clj-ml.data :as mld]
            [clj-ml.filters :as mlf]
            [clj-ml.utils :as clu]
            [clojure.java.io :as io]
            [incanter.charts :as c]
            [incanter.core :as i]
            [incanter.optimize :as o]
            [incanter.stats :as s]
            [incanter.svg :as svg]))

(defn ex-4-1
  "Visualize the Titanic dataset"
  []
  (i/view (load-data "titanic.tsv")))

(defn ex-4-2
  "Produce a frequency table for survivors by sex"
  []
  (->> (load-data "titanic.tsv")
       (frequency-table :count [:sex :survived])))

(defn ex-4-3
  "Convert frequency table to a nested map"
  []
  (->> (load-data "titanic.tsv")
       (frequency-map :count [:sex :survived])))

(defn ex-4-4
  "Calculate survival rate by sex"
  []
  (-> (load-data "titanic.tsv")
      (fatalities-by-sex)))

(defn ex-4-5
  "Calculate relative risk of perishing on the Titanic"
  []
  (let [proportions (-> (load-data "titanic.tsv")
                        (fatalities-by-sex))]
    (relative-risk (get proportions :male)
                   (get proportions :female))))

(defn ex-4-6
  "Calculate odds ratio"
  []
  (let [proportions (-> (load-data "titanic.tsv")
                        (fatalities-by-sex))]
    (odds-ratio (get proportions :male)
                (get proportions :female))))

(defn ex-4-7
  "Histogram of the bootstrap of females"
  []
  (let [passengers (concat (repeat 127 0)
                           (repeat 339 1))
        bootstrap (s/bootstrap passengers i/sum :size 10000)]
    (-> (c/histogram bootstrap
                     :x-label "Female Survivors"
                     :nbins 20)
        (i/view))))

(defn ex-4-8
  "Calculate SD"
  []
  (-> (concat (repeat 127 0)
              (repeat 339 1))
      (s/bootstrap i/sum :size 10000)
      (s/sd)))

(defn ex-4-9
  "Comparison of normal and binomial distributions"
  []
  (let [passengers (concat (repeat 127 0)
                           (repeat 339 1))
        bootstrap (s/bootstrap passengers i/sum :size 10000)
        binomial (fn [x]
                   (s/pdf-binomial x :size 466 :prob (/ 339 466)))
        normal (fn [x]
                 (s/pdf-normal x :mean 339 :sd 9.644))]
    (-> (c/histogram bootstrap
                     :nbins 20
                     :density true
                     :legend true)
        (c/add-function binomial 300 380
                        :series-label "Binomial")
        (c/add-function normal 300 380
                        :series-label "Normal")
        (i/view))))

(defn ex-4-10
  "Calculate the standard error proportion"
  []
  (let [survived (->> (load-data "titanic.tsv")
                      (frequency-map :count [:sex :survived]))
        n (reduce + (vals (get survived "female")))
        p (/ (get-in survived ["female" "y"]) n)]
    (standard-error-proportion p n)))

(defn ex-4-11
  "Check whether the survival rate difference is significant"
  []
  (let [dataset (load-data "titanic.tsv")
        proportions (fatalities-by-sex dataset)
        survived (frequency-map :count [:survived] dataset)
        total (reduce + (vals survived))
        pooled (/ (get survived "n") total)
        p-diff (- (get proportions :male)
                  (get proportions :female))
        z-stat (/ p-diff (standard-error-proportion pooled total))]
    (- 1 (s/cdf-normal (i/abs z-stat)))))

(defn ex-4-12
  "Check survival rate by class"
  []
  (->> (load-data "titanic.tsv")
       (frequency-table :count [:survived :pclass])))

(defn ex-4-13
  "Visualize survival rate by class"
  []
  (let [data (->> (load-data "titanic.tsv")
                  (frequency-table :count [:survived :pclass]))]
    (-> (c/stacked-bar-chart :pclass :count
                             :group-by :survived
                             :legend true
                             :data data)
        (i/view))))

(defn ex-4-14
  "Calculate expected frequencies"
  []
  (-> (load-data "titanic.tsv")
      (expected-frequencies)))

(defn ex-4-15
  "Calculate observed frequencies"
  []
  (-> (load-data "titanic.tsv")
      (observed-frequencies)))

(defn ex-4-16
  "Calculate chi-square"
  []
  (let [data (load-data "titanic.tsv")
        observed (observed-frequencies data)
        expected (expected-frequencies data)]
    (float (chisq-stat observed expected))))

(defn ex-4-17
  "Chi-square test"
  []
  (let [data (load-data "titanic.tsv")
        observed (observed-frequencies data)
        expected (expected-frequencies data)
        x2-stat (chisq-stat observed expected)]
    (s/cdf-chisq x2-stat :df 2 :lower-tail? false)))








