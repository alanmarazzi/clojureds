(ns cljds.ch5.examples
  (:require ;[abracad.avro :as avro]
            ;[cljds.ch5.data :refer :all]
            ;[cljds.ch5.hadoop :refer [hadoop-gradient-descent]]
            ;[cljds.ch5.tesser :refer :all]
            ;[cljds.ch5.parkour :refer [hadoop-sgd hadoop-extract-features]]
            [clojure.core.reducers :as r]
            ;[cljds.ch5.util :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [incanter.charts :as c]
            [incanter.core :as i]
            ;[iota]
            ;[parkour.conf :as conf]
            ;[parkour.io.text :as text]
            ;[tesser.core :as t]
            ;[tesser.hadoop :as h]
            ;[tesser.math :as m]
            ;[incanter.svg :as svg]
            ))

(defn ex-5-1
  []
  (-> (slurp "data/soi.csv")
      (str/split #"\n")
      (first)))

(defn ex-5-2
  []
  (-> (io/reader "data/soi.csv")
      (line-seq)
      (first)))

