(ns cljds.ch3.examples
  (:require [cljds.ch3.data :refer :all]
            [cljds.ch3.stats :refer :all]
            [clj-time.coerce :as coerce]
            [clj-time.core :as time]
            [incanter.charts :as c]
            [incanter.core :as i]
            [incanter.excel :as xls]
            [incanter.stats :as s]
            [incanter.svg :as svg]))


(defn ex-3-1
  []
  (i/view (athlete-data)))

(defn ex-3-2
  []
  (-> (remove nil? (i/$ "Height, cm" (athlete-data)))
      (c/histogram :nbins 20
                   :x-label "Height"
                   :y-label "Freq")
      (i/view)))

(defn ex-3-3
  []
  (-> (remove nil? (i/$ "Weight" (athlete-data)))
      (c/histogram :nbins 20
                   :x-label "Weight"
                   :y-label "Freq")
      (i/view)))

(defn ex-3-4
  "Check the skew of weights"
  []
  (->> (swimmer-data)
       (i/$ "Weight")
       (remove nil?)
       (s/skewness)))

(defn ex-3-5
  "Reduce skew with the log of weights"
  []
  (-> (remove nil? (i/$ "Weight" (athlete-data)))
      (i/log)
      (c/histogram :nbins 20)
      (i/view)))

(defn ex-3-6
  "Scatter plot of swimmers' height and weight"
  []
  (let [data (swimmer-data)
        heights (i/$ "Height, cm" data)
        weights (i/log (i/$ "Weight" data))]
    (-> (c/scatter-plot heights weights
                        :x-label "Height"
                        :y-label "Weight")
        (i/view))))

(defn ex-3-7
  "Same plot as previous but adding jitter"
  []
  (let [data (swimmer-data)
        heights (->> (i/$ "Height, cm" data)
                     (map (jitter 0.5)))
        weights (->> (i/$ "Weight" data)
                     (map (jitter 0.5))
                     (i/log))]
    (-> (c/scatter-plot heights weights)
        (i/view))))

(defn ex-3-8
  "Calculate correlation between heights and weights"
  []
  (let [data (swimmer-data)
        heights (i/$ "Height, cm" data)
        weights (i/log (i/$ "Weight" data))]
    (correlation heights weights)))

(defn ex-3-9
  "Calculate the p-value of the correlation"
  []
  (let [data (swimmer-data)
        heights (i/$ "Height, cm" data)
        weights (i/log (i/$ "Weight" data))
        t-value (t-statistic heights weights)
        df (- (count heights) 2)
        ; To get p-value we multiply by 2 since is 2 tailed
        p (* 2 (s/cdf-t t-value :df df :lower-tail? false))]
    (println "t-value " t-value)
    (println "p-value " p)))

(defn ex-3-10
  []
  (let [data (swimmer-data)
        heights (i/$ "Height, cm" data)
        weights (i/log (i/$ "Weight" data))
        interval (r-confidence-interval 1.96 heights weights)]
    (println "Confidence interval: " interval)))

(defn ex-3-11
  []
  (-> (c/function-plot celsius->fahreneit -10 40)
      (i/view)))

(defn ex-3-12
  "Calculate slope and intercept via OLS"
  []
  (let [data (swimmer-data)
        heights (i/$ "Height, cm" data)
        weights (i/log (i/$ "Weight" data))
        a (intercept heights weights)
        b (slope heights weights)]
    (println "Intercept: " a)
    (println "Slope: " b)))

(defn ex-3-13
  "Plot data and the regression line"
  []
  (let [data (swimmer-data)
        heights (->> (i/$ "Height, cm" data)
                     (map (jitter 0.5)))
        weights (i/log (i/$ "Weight" data))
        a (intercept heights weights)
        b (slope heights weights)]
    (-> (c/scatter-plot heights weights)
        (c/add-function (regression-line a b) 150 210)
        (i/view))))

(defn ex-3-14
  "Calculate and plot residuals"
  []
  (let [data (swimmer-data)
        heights (->> (i/$ "Height, cm" data)
                     (map (jitter 0.5)))
        weights (i/log (i/$ "Weight" data))
        a (intercept heights weights)
        b (slope heights weights)]
    (-> (c/scatter-plot heights (residuals a b heights weights))
        (c/add-function (constantly 0) 150 210)
        (i/view))))

(defn ex-3-15
  "Calculate R^2"
  []
  (let [data (swimmer-data)
        heights (i/$ "Height, cm" data)
        weights (i/log (i/$ "Weight" data))
        a (intercept heights weights)
        b (slope heights weights)]
    (r-squared a b heights weights)))

(defn ex-3-16
  "Build and output a matrix"
  []
  (->> (swimmer-data)
       (i/$ ["Height, cm" "Weight"])
       (i/to-matrix)))

(defn ex-3-18
  "Linear Regression via normal equation"
  []
  (let [data (swimmer-data)
        x (i/matrix (i/$ "Height, cm" data))
        y (i/matrix (i/log (i/$ "Weight" data)))]
    (normal-equation (add-bias x) y)))

(defn ex-3-20
  "Multiple linear regression"
  []
  (let [data (swimmer-data)
        x (->> data
               (feature-matrix ["Height, cm" "Age"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log)
               (i/matrix))]
    (normal-equation x y)))

(defn ex-3-21
  "Calculate R^2 for the multiple regression"
  []
  (let [data (swimmer-data)
        x (->> data
               (feature-matrix ["Height, cm" "Age"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log)
               (i/matrix))
        beta (normal-equation x y)]
    (r-squared beta x y)))

(defn ex-3-22
  "Calculated the adjusted R^2"
  []
  (let [data (swimmer-data)
        x (->> data
               (feature-matrix ["Height, cm" "Age"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log)
               (i/matrix))
        beta (normal-equation x y)]
    (matrix-adj-r-squared beta x y)))

(defn ex-3-23
  "Perform the f-test"
  []
  (let [data (swimmer-data)
        x (->> data
               (feature-matrix ["Height, cm" "Age"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log))
        beta (:coefs (s/linear-model y x :intercept false))]
    (f-test beta x y)))

(defn ex-3-24
  "Add sex as explanatory variable"
  []
  (let [data (->>  (swimmer-data)
                   (i/add-derived-column "Dummy MF"
                                         ["Sex"]
                                         dummy-mf))
        x (->> data
               (feature-matrix ["Height, cm" "Age" "Dummy MF"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log)
               (i/matrix))
        beta (normal-equation x y)]
    (matrix-adj-r-squared beta x y)))

(defn ex-3-25
  "Calculate Beta weight for each predictor"
  []
  (let [data (->> (swimmer-data)
                  (i/add-derived-column "Dummy MF"
                                        ["Sex"]
                                        dummy-mf))
        x (->> data
               (feature-matrix ["Height, cm" "Age" "Dummy MF"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log)
               (i/matrix))
        beta (normal-equation x y)]
    (beta-weight beta x y)))

(defn ex-3-26
  "Try to predict the weight of a swimmer"
  []
  (let [data (->> (swimmer-data)
                  (i/add-derived-column "Dummy MF"
                                        ["Sex"]
                                        dummy-mf)
                  (i/add-derived-column "Year of birth"
                                        ["Date of birth"]
                                        to-year))
        x (->> data
               (feature-matrix ["Height, cm" "Dummy MF" "Year of birth"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log)
               (i/matrix))
        beta (normal-equation x y)
        xspitz (i/matrix [1.0 183 1 1950])]
    (i/exp (predict beta xspitz))))

(defn ex-3-27
  "Calculate prediction interval for the prediction"
  []
  (let [data (->> (swimmer-data)
                  (i/add-derived-column "Dummy MF"
                                        ["Sex"]
                                        dummy-mf)
                  (i/add-derived-column "Year of birth"
                                        ["Date of birth"]
                                        to-year))
        x (->> data
               (feature-matrix ["Height, cm" "Dummy MF" "Year of birth"])
               (add-bias))
        y (->> data
               (i/$ "Weight")
               (i/log)
               (i/matrix))
        xspitz (i/matrix [1.0 183 1 1950])]
    (i/exp (prediction-interval x y xspitz))))
