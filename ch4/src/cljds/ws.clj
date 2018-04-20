;; gorilla-repl.fileformat = 1

;; **
;;; # Chapter 4 - Classification
;;; 
;;; 
;; **

;; @@
(ns cljds.ch4.ws
  (:require [clj-ml.classifiers :as cl]
            [clj-ml.data :as mld]
            [clj-ml.filters :as mlf]
            [clj-ml.utils :as clu]
            [clojure.java.io :as io]
            [incanter.io :as iio]
            [incanter.charts :as c]
            [incanter.core :as i]
            [incanter.optimize :as o]
            [incanter.stats :as s]
            [incanter.svg :as svg]
            [clojure.pprint :as pp])
  (:use [incanter-gorilla.render]
        [clojure.repl :only [doc source]]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Data Inspection
;;; 
;;; As usual we will load and check our dataset about the **Titanic**
;; **

;; @@
(defn load-data
  [file]
  (-> (io/resource file)
      (str)
      (iio/read-dataset :delim \tab :header true)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/load-data</span>","value":"#'cljds.ch4.ws/load-data"}
;; <=

;; @@
(->> (load-data "titanic.tsv")
     :rows
     (take 5)
     (map #(apply dissoc % [:home.dest :name]))
     pp/print-table)
;; @@
;; ->
;;; 
;;; | :pclass | :age |   :sex | :parch | :sibsp |    :fare | :embarked | :survived | :ticket |  :cabin | :body | :boat |
;;; |---------+------+--------+--------+--------+----------+-----------+-----------+---------+---------+-------+-------|
;;; |   first |   29 | female |      0 |      0 | 211.3375 |         S |         y |   24160 |      B5 |       |     2 |
;;; |   first |      |   male |      2 |      1 |   151.55 |         S |         y |  113781 | C22 C26 |       |    11 |
;;; |   first |    2 | female |      2 |      1 |   151.55 |         S |         n |  113781 | C22 C26 |       |       |
;;; |   first |   30 |   male |      2 |      1 |   151.55 |         S |         n |  113781 | C22 C26 |   135 |       |
;;; |   first |   25 | female |      2 |      1 |   151.55 |         S |         n |  113781 | C22 C26 |       |       |
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(defn frequency-table
  [sum-column group-columns dataset]
  (->> (i/$ group-columns dataset)
       (i/add-column sum-column (repeat 1))
       (i/$rollup :sum sum-column group-columns)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/frequency-table</span>","value":"#'cljds.ch4.ws/frequency-table"}
;; <=

;; @@
(->> (load-data "titanic.tsv")
     (frequency-table :count [:sex :survived])
     :rows
     pp/print-table)
;; @@
;; ->
;;; 
;;; |   :sex | :survived | :count |
;;; |--------+-----------+--------|
;;; | female |         y |    339 |
;;; |   male |         y |    161 |
;;; | female |         n |    127 |
;;; |   male |         n |    682 |
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Incanter starts to be annoying for working seriously with data, let's convert the dataset to a series of maps
;; **

;; @@
(defn frequency-map
  [sum-column group-cols dataset]
  (let [f (fn [freq-map row]
            (let [groups (map row group-cols)]
              (->> (get row sum-column)
                   (assoc-in freq-map groups))))]
    (->> (frequency-table sum-column group-cols dataset)
         :rows
         (reduce f {}))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/frequency-map</span>","value":"#'cljds.ch4.ws/frequency-map"}
;; <=

;; @@
(->> (load-data "titanic.tsv")
     (frequency-map :count [:sex :survived]))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;female&quot;</span>","value":"\"female\""},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;y&quot;</span>","value":"\"y\""},{"type":"html","content":"<span class='clj-long'>339</span>","value":"339"}],"value":"[\"y\" 339]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;n&quot;</span>","value":"\"n\""},{"type":"html","content":"<span class='clj-long'>127</span>","value":"127"}],"value":"[\"n\" 127]"}],"value":"{\"y\" 339, \"n\" 127}"}],"value":"[\"female\" {\"y\" 339, \"n\" 127}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;male&quot;</span>","value":"\"male\""},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;y&quot;</span>","value":"\"y\""},{"type":"html","content":"<span class='clj-long'>161</span>","value":"161"}],"value":"[\"y\" 161]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;n&quot;</span>","value":"\"n\""},{"type":"html","content":"<span class='clj-long'>682</span>","value":"682"}],"value":"[\"n\" 682]"}],"value":"{\"y\" 161, \"n\" 682}"}],"value":"[\"male\" {\"y\" 161, \"n\" 682}]"}],"value":"{\"female\" {\"y\" 339, \"n\" 127}, \"male\" {\"y\" 161, \"n\" 682}}"}
;; <=

;; @@
(defn fatalities-by-sex
  [dataset]
  (let [totals (frequency-map :count [:sex] dataset)
        groups (frequency-map :count [:sex :survived] dataset)]
    {:male   (/ (get-in groups ["male" "n"])
                (get totals "male"))
     :female (/ (get-in groups ["female" "n"])
                (get totals "female"))}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/fatalities-by-sex</span>","value":"#'cljds.ch4.ws/fatalities-by-sex"}
;; <=

;; @@
(-> (load-data "titanic.tsv")
    (fatalities-by-sex))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:male</span>","value":":male"},{"type":"html","content":"<span class='clj-ratio'>682/843</span>","value":"682/843"}],"value":"[:male 682/843]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:female</span>","value":":female"},{"type":"html","content":"<span class='clj-ratio'>127/466</span>","value":"127/466"}],"value":"[:female 127/466]"}],"value":"{:male 682/843, :female 127/466}"}
;; <=

;; **
;;; At this point we can calculate the **relative risk** by sex of being on the Titanic and the **odds ratio**
;; **

;; @@
(defn relative-risk
  [p1 p2]
  (float (/ p1 p2)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/relative-risk</span>","value":"#'cljds.ch4.ws/relative-risk"}
;; <=

;; @@
(let [proportions (-> (load-data "titanic.tsv")
                      (fatalities-by-sex))]
  (relative-risk (get proportions :male)
                 (get proportions :female)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>2.9685133</span>","value":"2.9685133"}
;; <=

;; @@
(defn odds-ratio
  [p1 p2]
  (float
    (/ (* p1 (- 1 p2))
       (* p2 (- 1 p1)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/odds-ratio</span>","value":"#'cljds.ch4.ws/odds-ratio"}
;; <=

;; @@
(let [proportions (-> (load-data "titanic.tsv")
                      (fatalities-by-sex))]
  (odds-ratio (get proportions :male)
              (get proportions :female)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>11.307184</span>","value":"11.307184"}
;; <=

;; **
;;; ## Standard Error with Bootstrapping
;; **

;; @@
(let [passengers (concat (repeat 127 0)
                         (repeat 339 1))
      bootstrap (s/bootstrap passengers i/sum :size 10000)]
  (-> (c/histogram bootstrap
                   :x-label "Female Survivors"
                   :nbins 20)
      (chart-view)))
;; @@
;; =>
;;; {"type":"html","content":"<img src=\"data:image/PNG;base64,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\"/>","value":"#incanter_gorilla.render.ChartView{:content #object[org.jfree.chart.JFreeChart 0x1334ddc2 \"org.jfree.chart.JFreeChart@1334ddc2\"], :opts nil}"}
;; <=

;; @@
(-> (concat (repeat 127 0)
            (repeat 339 1))
    (s/bootstrap i/sum :size 10000)
    (s/sd))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>9.60538619392229</span>","value":"9.60538619392229"}
;; <=

;; **
;;; ## Binomial Distribution
;; **

;; @@
(let [passengers (concat (repeat 127 0)
                         (repeat 339 1))
      bootstrap (s/bootstrap passengers i/sum :size 10000)
      binomial (fn [x]
                 (s/pdf-binomial x :size 466 :prob (/ 339 466)))
      normal (fn [x]
               (s/pdf-normal x :mean 339 :sd 9.57))]
  (-> (c/histogram bootstrap
                   :x-label "Female Survivors"
                   :series-label "Bootstrap"
                   :nbins 20
                   :density true
                   :legend true)
      (c/add-function binomial 300 380
                      :series-label "Binomial")
      (c/add-function normal 300 380
                      :series-label "Normal")
      (chart-view)))
;; @@
;; =>
;;; {"type":"html","content":"<img src=\"data:image/PNG;base64,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\"/>","value":"#incanter_gorilla.render.ChartView{:content #object[org.jfree.chart.JFreeChart 0x53b23161 \"org.jfree.chart.JFreeChart@53b23161\"], :opts nil}"}
;; <=

;; **
;;; ## Standard Error
;; **

;; @@
(defn standard-error-proportion
  [p n]
  (-> (- 1 p)
      (* p)
      (/ n)
      (i/sqrt)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/standard-error-proportion</span>","value":"#'cljds.ch4.ws/standard-error-proportion"}
;; <=

;; @@
(let [survived (->> (load-data "titanic.tsv")
                    (frequency-map :count [:sex :survived]))
      n (reduce + (vals (get survived "female")))
      p (/ (get-in survived ["female" "y"]) n)]
  (standard-error-proportion p n))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.020626371453251833</span>","value":"0.020626371453251833"}
;; <=

;; **
;;; ## Chi-squared test
;; **

;; @@
(def data-loaded (load-data "titanic.tsv"))

(->> data-loaded
     (frequency-table :count [:survived :pclass])
     :rows
     pp/print-table)
;; @@
;; ->
;;; 
;;; | :survived | :pclass | :count |
;;; |-----------+---------+--------|
;;; |         y |   first |    200 |
;;; |         n |   first |    123 |
;;; |         n |  second |    158 |
;;; |         y |  second |    119 |
;;; |         n |   third |    528 |
;;; |         y |   third |    181 |
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(let [data (->> data-loaded
                (frequency-table :count [:survived :pclass]))]
  (-> (c/stacked-bar-chart :pclass :count
                           :group-by :survived
                           :legend true
                           :data data)
      (chart-view)))
;; @@
;; =>
;;; {"type":"html","content":"<img src=\"data:image/PNG;base64,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\"/>","value":"#incanter_gorilla.render.ChartView{:content #object[org.jfree.chart.JFreeChart 0x175924ee \"org.jfree.chart.JFreeChart@175924ee\"], :opts nil}"}
;; <=

;; @@
(defn expected-frequencies
  [data]
  (let [as (vals (frequency-map :count [:survived] data))
        bs (vals (frequency-map :count [:pclass] data))
        total (-> data :rows count)]
    (for [a as
          b bs]
      (* a (/ b total)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/expected-frequencies</span>","value":"#'cljds.ch4.ws/expected-frequencies"}
;; <=

;; @@
(-> data-loaded
    (expected-frequencies))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-ratio'>9500/77</span>","value":"9500/77"},{"type":"html","content":"<span class='clj-ratio'>138500/1309</span>","value":"138500/1309"},{"type":"html","content":"<span class='clj-ratio'>354500/1309</span>","value":"354500/1309"},{"type":"html","content":"<span class='clj-ratio'>15371/77</span>","value":"15371/77"},{"type":"html","content":"<span class='clj-ratio'>224093/1309</span>","value":"224093/1309"},{"type":"html","content":"<span class='clj-ratio'>573581/1309</span>","value":"573581/1309"}],"value":"(9500/77 138500/1309 354500/1309 15371/77 224093/1309 573581/1309)"}
;; <=

;; **
;;; ### Chi-squared Statistic
;;; 
;;; It simply measures how far the actual frequencies differ from those calculated under the assumption of independence:
;;; 
;;; $$
;;; \Chi^2=\sum\_{ij}\frac{(f\_{ij}-F\_{ij})^2}{F\_{ij}}
;;; $$
;;; 
;;; @@F\_{ij}@@ is the expected frequency assuming independence for categories @@i@@ and @@j@@
;; **

;; @@
(defn observed-frequencies [data]
  (let [as (frequency-map :count [:survived] data)
        bs (frequency-map :count [:pclass] data)
        actual (frequency-map :count [:survived :pclass] data)]
    (for [a (keys as)
          b (keys bs)]
(get-in actual [a b]))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/observed-frequencies</span>","value":"#'cljds.ch4.ws/observed-frequencies"}
;; <=

;; @@
(-> data-loaded
    (observed-frequencies))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"},{"type":"html","content":"<span class='clj-long'>119</span>","value":"119"},{"type":"html","content":"<span class='clj-long'>181</span>","value":"181"},{"type":"html","content":"<span class='clj-long'>123</span>","value":"123"},{"type":"html","content":"<span class='clj-long'>158</span>","value":"158"},{"type":"html","content":"<span class='clj-long'>528</span>","value":"528"}],"value":"(200 119 181 123 158 528)"}
;; <=

;; @@
(defn chisq-stat
  [observed expected]
  (let [f (fn [observed expected]
            (/ (i/sq (- observed expected)) expected))]
    (reduce + (map f observed expected))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/chisq-stat</span>","value":"#'cljds.ch4.ws/chisq-stat"}
;; <=

;; @@
(let [observed (observed-frequencies data-loaded)
      expected (expected-frequencies data-loaded)]
  (float (chisq-stat observed expected)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>127.85915</span>","value":"127.85915"}
;; <=

;; @@
(let [observed (observed-frequencies data-loaded)
      expected (expected-frequencies data-loaded)
      x2-stat (chisq-stat observed expected)]
  (s/cdf-chisq x2-stat :df 2 :lower-tail? false))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>1.7208259588255803E-28</span>","value":"1.7208259588255803E-28"}
;; <=

;; **
;;; ## Logistic Regression
;;; 
;;; ### Sigmoid Function
;; **

;; @@
(defn sigmoid
  [coefs]
  (let [bt (i/trans coefs)
        z (fn [x] (- (first (i/mmult bt x))))]
    (fn [x]
      (/ 1
         (+ 1
            (i/exp (z x)))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/sigmoid</span>","value":"#'cljds.ch4.ws/sigmoid"}
;; <=

;; @@
(let [f (sigmoid [0])]
  (println (f [1]))
  (println (f [-1]))
  (println (f [42])))
;; @@
;; ->
;;; 0.5
;;; 0.5
;;; 0.5
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(let [f (sigmoid [0.2])
      g (sigmoid [-0.2])]
  (println (f [5]))
  (println (g [5])))
;; @@
;; ->
;;; 0.7310585786300049
;;; 0.2689414213699951
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(defn logistic-cost
  [ys y-hats]
  (let [cost (fn [y y-hat]
               (if (zero? y)
                 (- (i/log (- 1 y-hat)))
                 (- (i/log y-hat))))]
    (s/mean (map cost ys y-hats))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/logistic-cost</span>","value":"#'cljds.ch4.ws/logistic-cost"}
;; <=

;; **
;;; ## Gradient Descent
;; **

;; @@
(let [f (fn [[x]]
          (i/sq x))
      init [10]]
  (:value (o/minimize f init)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 1x1 matrix\n -------------\n-2.50e-12 \n</span>","value":" A 1x1 matrix\n -------------\n-2.50e-12 \n"}
;; <=

;; **
;;; ## Implementation of Logistic Regression
;; **

;; @@
(defn logistic-regression
  [ys xs]
  (let [cost-fn (fn [coefs]
                  (let [classify (sigmoid coefs)
                        y-hats (map (comp
                                      classify
                                      i/trans) xs)]
                    (logistic-cost ys y-hats)))
        init (repeat (i/ncol xs) 0.0)]
    (o/minimize cost-fn init)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/logistic-regression</span>","value":"#'cljds.ch4.ws/logistic-regression"}
;; <=

;; @@
(defn add-dummy
  [column-name from-column value dataset]
  (i/add-derived-column column-name
                        [from-column]
                        #(if (= % value) 1 0)
                        dataset))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/add-dummy</span>","value":"#'cljds.ch4.ws/add-dummy"}
;; <=

;; @@
(defn matrix-ds []
  (->> data-loaded
       (add-dummy :dummy-survived :survived "y")
       (i/add-column :bias (repeat 1.0))
       (add-dummy :dummy-mf :sex "male")
       (add-dummy :dummy-1 :pclass "first")
       (add-dummy :dummy-2 :pclass "second")
       (add-dummy :dummy-3 :pclass "third")
       (i/$ [:dummy-survived :bias :dummy-mf
             :dummy-1 :dummy-2 :dummy-3])
       (i/to-matrix)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/matrix-ds</span>","value":"#'cljds.ch4.ws/matrix-ds"}
;; <=

;; @@
(matrix-ds)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 1309x6 matrix\n ----------------\n 1.00e+00  1.00e+00  0.00e+00  .  1.00e+00  0.00e+00  0.00e+00 \n 1.00e+00  1.00e+00  1.00e+00  .  1.00e+00  0.00e+00  0.00e+00 \n 0.00e+00  1.00e+00  0.00e+00  .  1.00e+00  0.00e+00  0.00e+00 \n ... \n 0.00e+00  1.00e+00  1.00e+00  .  0.00e+00  0.00e+00  1.00e+00 \n 0.00e+00  1.00e+00  1.00e+00  .  0.00e+00  0.00e+00  1.00e+00 \n 0.00e+00  1.00e+00  1.00e+00  .  0.00e+00  0.00e+00  1.00e+00 \n</span>","value":" A 1309x6 matrix\n ----------------\n 1.00e+00  1.00e+00  0.00e+00  .  1.00e+00  0.00e+00  0.00e+00 \n 1.00e+00  1.00e+00  1.00e+00  .  1.00e+00  0.00e+00  0.00e+00 \n 0.00e+00  1.00e+00  0.00e+00  .  1.00e+00  0.00e+00  0.00e+00 \n ... \n 0.00e+00  1.00e+00  1.00e+00  .  0.00e+00  0.00e+00  1.00e+00 \n 0.00e+00  1.00e+00  1.00e+00  .  0.00e+00  0.00e+00  1.00e+00 \n 0.00e+00  1.00e+00  1.00e+00  .  0.00e+00  0.00e+00  1.00e+00 \n"}
;; <=

;; @@
(let [data (matrix-ds)
      ys (i/$ 0 data)
      xs (i/$ [:not 0] data)]
  (:value (logistic-regression ys xs)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 5x1 matrix\n -------------\n 9.31e-01 \n-2.52e+00 \n 1.18e+00 \n 2.97e-01 \n-5.45e-01 \n</span>","value":" A 5x1 matrix\n -------------\n 9.31e-01 \n-2.52e+00 \n 1.18e+00 \n 2.97e-01 \n-5.45e-01 \n"}
;; <=

;; **
;;; ## Evaluate Logistic Regression Results
;; **

;; @@
(defn logistic-class [x]
  (if (>= x 0.5) 1 0))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/logistic-class</span>","value":"#'cljds.ch4.ws/logistic-class"}
;; <=

;; @@
(let [data       (matrix-ds)
      ys         (i/$ 0 data)
      xs         (i/$ [:not 0] data)
      coefs      (:value (logistic-regression ys xs))
      classifier (comp
                   logistic-class
                   (sigmoid coefs)
                   i/trans)]
  (println "Observed:  " (map int (take 10 ys)))
  (println "Predicted: " (map classifier (take 10 xs))))
;; @@
;; ->
;;; Observed:   (1 1 0 0 0 1 1 0 1 0)
;;; Predicted:  (1 0 1 0 1 0 1 0 1 0)
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(let [data       (matrix-ds)
      ys         (i/$ 0 data)
      xs         (i/$ [:not 0] data)
      coefs      (:value (logistic-regression ys xs))
      classifier (comp
                   logistic-class
                   (sigmoid coefs)
                   i/trans)
      y-hats (map classifier xs)]
  (frequencies (map = y-hats (map int ys))))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-unkown'>true</span>","value":"true"},{"type":"html","content":"<span class='clj-long'>1021</span>","value":"1021"}],"value":"[true 1021]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-unkown'>false</span>","value":"false"},{"type":"html","content":"<span class='clj-long'>288</span>","value":"288"}],"value":"[false 288]"}],"value":"{true 1021, false 288}"}
;; <=

;; **
;;; ## Confusion Matrix
;; **

;; @@
(defn confusion-matrix
  [ys y-hats]
  (let [classes (into #{} (concat ys y-hats))
        confusion (frequencies (map vector ys y-hats))]
    (i/dataset (cons nil classes)
               (for [x classes]
                 (cons x
                       (for [y classes]
                         (get confusion [x y])))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/confusion-matrix</span>","value":"#'cljds.ch4.ws/confusion-matrix"}
;; <=

;; @@
(let [data       (matrix-ds)
      ys         (i/$ 0 data)
      xs         (i/$ [:not 0] data)
      coefs      (:value (logistic-regression ys xs))
      classifier (comp
                   logistic-class
                   (sigmoid coefs)
                   i/trans)
      y-hats (map classifier xs)]
  (pp/print-table (:rows (confusion-matrix (map int ys) y-hats))))
;; @@
;; ->
;;; 
;;; |   |   0 |   1 |
;;; |---+-----+-----|
;;; | 0 | 682 | 127 |
;;; | 1 | 161 | 339 |
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; # Naive Bayes
;;; 
;;; To implement a **Naive Bayes** classifier we have to start by calculating the number of examples corresponding to each value of each feature for each class
;; **

;; @@
(defn inc-class-total
  [model class]
  (update-in model [:classes class :n] (fnil inc 0)))

(defn inc-predictors-count-fn
  [row class]
  (fn [model attr]
    (let [val (get row attr)]
      (update-in model [:classes class :predictors attr val] (fnil inc 0)))))

(defn assoc-row-fn
  [class-attr predictors]
  (fn [model row]
    (let [class (get row class-attr)]
      (reduce (inc-predictors-count-fn row class)
              (inc-class-total model class)
              predictors))))

(defn bayes-classifier
  [class-attr predictors dataset]
  (reduce (assoc-row-fn class-attr predictors) {:n (count dataset)} dataset))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/bayes-classifier</span>","value":"#'cljds.ch4.ws/bayes-classifier"}
;; <=

;; @@
(inc-class-total {} 1)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:classes</span>","value":":classes"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:n</span>","value":":n"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[:n 1]"}],"value":"{:n 1}"}],"value":"[1 {:n 1}]"}],"value":"{1 {:n 1}}"}],"value":"[:classes {1 {:n 1}}]"}],"value":"{:classes {1 {:n 1}}}"}
;; <=

;; @@
(->> (load-data "titanic.tsv")
     (:rows)
     (bayes-classifier :survived [:sex :pclass])
     pp/pprint)
;; @@
;; ->
;;; {:n 1309,
;;;  :classes
;;;  {&quot;y&quot;
;;;   {:n 500,
;;;    :predictors
;;;    {:sex {&quot;female&quot; 339, &quot;male&quot; 161},
;;;     :pclass {&quot;first&quot; 200, &quot;second&quot; 119, &quot;third&quot; 181}}},
;;;   &quot;n&quot;
;;;   {:n 809,
;;;    :predictors
;;;    {:sex {&quot;female&quot; 127, &quot;male&quot; 682},
;;;     :pclass {&quot;first&quot; 123, &quot;second&quot; 158, &quot;third&quot; 528}}}}}
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(defn posterior-probability
  [model test class-attr]
  (let [observed (get-in model [:classes class-attr])
        prior (/ (:n observed)
                 (:n model))]
    (apply * prior
           (for [[predictor value] test]
             (/ (get-in observed [:predictors predictor value])
                (:n observed))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/posterior-probability</span>","value":"#'cljds.ch4.ws/posterior-probability"}
;; <=

;; @@
(defn bayes-classify
  [model test]
  (let [probability (partial posterior-probability model test)
        classes     (keys (:classes model))]
    (apply max-key probability classes)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/bayes-classify</span>","value":"#'cljds.ch4.ws/bayes-classify"}
;; <=

;; @@
(let [model (->> data-loaded
                 :rows
                 (bayes-classifier :survived [:sex :pclass]))]
  (println "3rd class male:"
           (bayes-classify model {:sex "male" :pclass "third"}))
  (println "1st class female:"
           (bayes-classify model {:sex "female" :pclass "first"})))
;; @@
;; ->
;;; 3rd class male: n
;;; 1st class female: y
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(let [data (:rows data-loaded)
      model (bayes-classifier :survived [:sex :pclass] data)
      test (fn [test]
             (= (:survived test)
                (bayes-classify model
                                (select-keys test [:sex :class]))))
      results (frequencies (map test data))]
  (/ (get results true)
     (apply + (vals results))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-ratio'>1021/1309</span>","value":"1021/1309"}
;; <=

;; @@
(let [data (:rows data-loaded)
      model (bayes-classifier :survived [:sex :pclass] data)
      classify (fn [test]
                 (->> (select-keys test [:sex :pclass])
                      (bayes-classify model)))
      ys (map :survived data)
      y-hats (map classify data)]
  (pp/print-table (:rows (confusion-matrix ys y-hats))))
;; @@
;; ->
;;; 
;;; |   |   n |   y |
;;; |---+-----+-----|
;;; | n | 682 | 127 |
;;; | y | 161 | 339 |
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Decision Trees
;;; 
;;; ### Information
;;; 
;;; Let's say someone randomly picks a card from a deck and challenges us to guess what card he picked. To help us he offers to answer a questiuon with a yes or no. What question do we pose?
;;; 
;;; * Is it red?
;;; * Is it a picture card?
;;; 
;;; There are **26** red cards in a deck, so the probability of randomly choosing one from a 52 cards deck is @@\frac{1}{2}@@. There are **12** picture cards in a deck, so the probability of randomly picking one is @@\frac{3}{13}@@.
;;; 
;;; The **information** associated with a single event is:
;;; 
;;; $$I(e)=-log\_2P(e)$$
;; **

;; @@
(defn information
  [p]
  (- (i/log2 p)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/information</span>","value":"#'cljds.ch4.ws/information"}
;; <=

;; @@
(let [i-red (information 1/2)
      i-pic (information 3/13)]
  (println "Information red:     " i-red)
  (println "Information picture: " i-pic))
;; @@
;; ->
;;; Information red:      1.0
;;; Information picture:  2.115477217419936
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Since the picture card has the lower probability, it carries more information than knowing that the card is red. We can look at the information result as in **bits**: with 1 bit we can represent a 50/50 split, while we need 2 bits to represent picture cards.
;;; 
;;; Anyway, what happens if the answer to both of the questions above is no?
;; **

;; @@
(let [i-red (information 1/2)
      i-pic (information 10/13)]
  (println "Information red:     " i-red)
  (println "Information picture: " i-pic))
;; @@
;; ->
;;; Information red:      1.0
;;; Information picture:  0.3785116232537299
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; The issue is we don't know whether the answer to the question will be yes or no. So how we decide which question to pose?
;;; 
;;; ### Entropy
;;; 
;;; **Entropy** can be calculated as:
;;; 
;;; $$H(X)=\sum{P(x\_i)I(P(x\_i))}$$
;;; 
;;; @@P(x)@@ is the probability of @@x@@ occurring and @@I(P(x))@@ is the information content of @@x@@.
;; **

;; @@
(defn entropy
  [xs]
  (let [n (count xs)
        f (fn [x]
            (let [p (/ x n)]
              (* p (information p))))]
    (->> (frequencies xs)
         (vals)
         (map f)
         (reduce +))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/entropy</span>","value":"#'cljds.ch4.ws/entropy"}
;; <=

;; @@
(let [red-black (concat (repeat 26 1)
                        (repeat 26 0))]
  (entropy red-black))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>1.0</span>","value":"1.0"}
;; <=

;; @@
(let [pic-not-pic (concat (repeat 12 1)
                          (repeat 40 0))]
  (entropy pic-not-pic))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.7793498372920853</span>","value":"0.7793498372920853"}
;; <=

;; **
;;; Entropy is a measure of uncertainty, the lower it is, the better. So, we should stick with the picture-not-picture question.
;;; 
;;; The nice thing is that entropy doesn't work only with numbers
;; **

;; @@
(entropy "mississippi")
(entropy "yellowstone")
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>2.9139770731827523</span>","value":"2.9139770731827523"}
;; <=

;; **
;;; ### Information Gain
;;; 
;;; Let's say we pick a passenger of the Titanic at random and we have to guess whether they survived or not. The rules are the same as before, we have 2 questions:
;;; 
;;; * Sex of the person
;;; * Class they were traveling in
;;; 
;;; To know which is the better question we have to know the **information gain** which is measured as entropy before and after we learn the new information.
;; **

;; @@
(->> data-loaded
     :rows
     (map :survived)
     (entropy))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.9594221708628148</span>","value":"0.9594221708628148"}
;; <=

;; **
;;; If we consider the entropy of survival when split by sex, we have to use the **weighted entropy**
;; **

;; @@
(defn weighted-entropy
  [groups]
  (let [n (count (apply concat groups))
        e (fn [group]
            (* (entropy group)
               (/ (count group) n)))]
    (->> (map e groups)
         (reduce +))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/weighted-entropy</span>","value":"#'cljds.ch4.ws/weighted-entropy"}
;; <=

;; @@
(->> data-loaded
     :rows
     (group-by :sex)
     vals
     (map (partial map :survived))
     (weighted-entropy))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.7539172981427391</span>","value":"0.7539172981427391"}
;; <=

;; @@
(defn information-gain
  [groups]
  (- (entropy (apply concat groups))
     (weighted-entropy groups)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/information-gain</span>","value":"#'cljds.ch4.ws/information-gain"}
;; <=

;; @@
(->> data-loaded
     :rows
     (group-by :pclass)
     vals
     (map (partial map :survived))
     (information-gain))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.0704074128460408</span>","value":"0.0704074128460408"}
;; <=

;; **
;;; We can see that knowing sex is more useful than knowing the class.
;;; 
;;; ## Build a tree
;;; 
;;; By using the already defined functions we can already construct a tree classifier.
;;; 
;;; We start by building a general purpose way to calculate information gain for a specific predictor.
;; **

;; @@
(defn gain-for-predictor
  [class-attr xs predictor]
  (let [grouped-classes (->> (group-by predictor xs)
                             vals
                             (map (partial map class-attr)))]
    (information-gain grouped-classes)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/gain-for-predictor</span>","value":"#'cljds.ch4.ws/gain-for-predictor"}
;; <=

;; **
;;; Next, we calculate the best predictor for a given set of rows
;; **

;; @@
(defn best-predictor
  [class-attr predictors xs]
  (let [gain (partial gain-for-predictor class-attr xs)]
    (when (seq predictors)
      (apply max-key gain predictors))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/best-predictor</span>","value":"#'cljds.ch4.ws/best-predictor"}
;; <=

;; @@
(->> data-loaded
     :rows
     (best-predictor :survived [:sex :pclass]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-keyword'>:sex</span>","value":":sex"}
;; <=

;; **
;;; By applying the same logic recursively, we can build a decision tree. But first let's define a function that will return the modal class given a sequence of data
;; **

;; @@
(defn modal-class
  [classes]
  (->> (frequencies classes)
       (apply max-key val)
       (key)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/modal-class</span>","value":"#'cljds.ch4.ws/modal-class"}
;; <=

;; @@
(defn map-vals [f coll]
(into {} (map (fn [[k v]] [k (f v)]) coll)))

(defn decision-tree
  [class-attr predictors xs]
  (let [classes (map class-attr xs)]
    (if (zero? (entropy classes))
      (first classes)
      (if-let [predictor (best-predictor class-attr
                                         predictors xs)]
        (let [predictors (remove #{predictor} predictors)
              tree-branch (partial decision-tree
                                   class-attr predictors)]
          (->> (group-by predictor xs)
               (map-vals tree-branch)
               (vector predictor)))
        (modal-class classes)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/decision-tree</span>","value":"#'cljds.ch4.ws/decision-tree"}
;; <=

;; @@
(->> data-loaded
     :rows
     (decision-tree :survived [:pclass :sex])
     (pp/pprint))
;; @@
;; ->
;;; [:sex
;;;  {&quot;female&quot; [:pclass {&quot;first&quot; &quot;y&quot;, &quot;second&quot; &quot;y&quot;, &quot;third&quot; &quot;n&quot;}],
;;;   &quot;male&quot; [:pclass {&quot;first&quot; &quot;n&quot;, &quot;second&quot; &quot;n&quot;, &quot;third&quot; &quot;n&quot;}]}]
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(defn age-categories
  [age]
  (cond
    (nil? age) "unknown"
    (< age 13) "child"
    :default "adult"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/age-categories</span>","value":"#'cljds.ch4.ws/age-categories"}
;; <=

;; @@
(->> (i/transform-col data-loaded :age age-categories)
     :rows
     (decision-tree :survived [:pclass :sex :age])
     pp/pprint)
;; @@
;; ->
;;; [:sex
;;;  {&quot;female&quot;
;;;   [:pclass
;;;    {&quot;first&quot; [:age {&quot;adult&quot; &quot;y&quot;, &quot;child&quot; &quot;n&quot;, &quot;unknown&quot; &quot;y&quot;}],
;;;     &quot;second&quot; [:age {&quot;adult&quot; &quot;y&quot;, &quot;child&quot; &quot;y&quot;, &quot;unknown&quot; &quot;y&quot;}],
;;;     &quot;third&quot; [:age {&quot;adult&quot; &quot;n&quot;, &quot;child&quot; &quot;n&quot;, &quot;unknown&quot; &quot;y&quot;}]}],
;;;   &quot;male&quot;
;;;   [:age
;;;    {&quot;unknown&quot; [:pclass {&quot;first&quot; &quot;n&quot;, &quot;second&quot; &quot;n&quot;, &quot;third&quot; &quot;n&quot;}],
;;;     &quot;adult&quot; [:pclass {&quot;first&quot; &quot;n&quot;, &quot;second&quot; &quot;n&quot;, &quot;third&quot; &quot;n&quot;}],
;;;     &quot;child&quot; [:pclass {&quot;first&quot; &quot;y&quot;, &quot;second&quot; &quot;y&quot;, &quot;third&quot; &quot;n&quot;}]}]}]
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Prediction with decision trees
;; **

;; @@
(defn tree-classify
  [model test]
  (if (vector? model)
    (let [[predictor branches] model
          branch (get branches (get test predictor))]
      (recur branch test))
    model))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/tree-classify</span>","value":"#'cljds.ch4.ws/tree-classify"}
;; <=

;; @@
(let [tree (->> (i/transform-col data-loaded :age age-categories)
     	   	    :rows
     			(decision-tree :survived [:pclass :sex :age]))
      test {:sex "male" :pclass "second" :age "child"}]
  (tree-classify tree test))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;y&quot;</span>","value":"\"y\""}
;; <=

;; @@
(let [data (-> data-loaded
               (i/transform-col :age age-categories)
               :rows)
      tree (decision-tree :survived [:pclass :sex :age] data)]
  (pp/print-table (:rows (confusion-matrix (map :survived data)
                                           (map (partial tree-classify tree) data)))))
;; @@
;; ->
;;; 
;;; |   |   n |   y |
;;; |---+-----+-----|
;;; | n | 763 |  46 |
;;; | y | 219 | 281 |
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Clj-ml
;; **

;; @@
(defn to-weka
  [dataset]
  (let [attributes [{:survived ["y" "n"]}
                    {:pclass ["first" "second" "third"]}
                    {:sex ["male" "female"]}
                    :age
                    :fare]
        vectors (->> dataset
                     (i/$ [:survived :pclass :sex :age :fare])
                     (i/to-vect))]
    (mld/make-dataset :titanic-weka attributes vectors
                      {:class :survived})))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch4.ws/to-weka</span>","value":"#'cljds.ch4.ws/to-weka"}
;; <=

;; **
;;; ### C4.5
;; **

;; @@
(let [dataset (to-weka data-loaded)
      classifier (-> (cl/make-classifier :decision-tree :c45)
                     (cl/classifier-train dataset))
      classify (partial cl/classifier-classify classifier)
      ys (map str (mld/dataset-class-values dataset))
      y-hats (map name (map classify dataset))]
  (println "Confusion:" (confusion-matrix ys y-hats)))
;; @@
;; ->
;;; Confusion: 
;;; |   |   n |   y |
;;; |---+-----+-----|
;;; | n | 712 |  97 |
;;; | y | 153 | 347 |
;;; 
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Dealing with overfitting
;; **

;; @@
(let [[test-set train-set] (-> data-loaded
                               to-weka
                               (mld/do-split-dataset :percentage 30))
      classifier (-> (cl/make-classifier :decision-tree :c45)
                     (cl/classifier-train train-set))
      classify (partial cl/classifier-classify classifier)
      ys (map str (mld/dataset-class-values test-set))
      y-hats (map name (map classify test-set))]
  (println (confusion-matrix ys y-hats)))
;; @@
;; ->
;;; 
;;; |   |   n |   y |
;;; |---+-----+-----|
;;; | n | 152 |   9 |
;;; | y |  65 | 167 |
;;; 
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Cross-validation
;; **

;; @@
(let [dataset (-> data-loaded
                  to-weka)
      classifier (-> (cl/make-classifier :decision-tree :c45)
                     (cl/classifier-train dataset))
      evaluation (cl/classifier-evaluate classifier
                                         :cross-validation
                                         dataset 10)]
  (println (:confusion-matrix evaluation))
  (println (:summary evaluation)))
;; @@
;; ->
;;; === Confusion Matrix ===
;;; 
;;;    a   b   &lt;-- classified as
;;;  347 153 |   a = y
;;;   98 711 |   b = n
;;; 
;;; 
;;; Correctly Classified Instances        1058               80.8251 %
;;; Incorrectly Classified Instances       251               19.1749 %
;;; Kappa statistic                          0.5852
;;; Mean absolute error                      0.2832
;;; Root mean squared error                  0.3785
;;; Relative absolute error                 59.9669 %
;;; Root relative squared error             77.8977 %
;;; Coverage of cases (0.95 level)          99.3888 %
;;; Mean rel. region size (0.95 level)      94.194  %
;;; Total Number of Instances             1309     
;;; 
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Ensemble Learning
;; **

;; @@
(let [dataset (->> data-loaded
                   to-weka
                   (mlf/make-apply-filter
                     :replace-missing-values {}))
      classifier (cl/make-classifier :decision-tree
                                     :random-forest)
      evaluation (cl/classifier-evaluate classifier
                                         :cross-validation
                                         dataset 10)]
  (println (:confusion-matrix evaluation))
  (println (:summary evaluation)))
;; @@
;; ->
;;; === Confusion Matrix ===
;;; 
;;;    a   b   &lt;-- classified as
;;;  361 139 |   a = y
;;;  128 681 |   b = n
;;; 
;;; 
;;; Correctly Classified Instances        1042               79.6028 %
;;; Incorrectly Classified Instances       267               20.3972 %
;;; Kappa statistic                          0.5662
;;; Mean absolute error                      0.2387
;;; Root mean squared error                  0.3924
;;; Relative absolute error                 50.5437 %
;;; Root relative squared error             80.7705 %
;;; Coverage of cases (0.95 level)          94.4996 %
;;; Mean rel. region size (0.95 level)      74.9045 %
;;; Total Number of Instances             1309     
;;; 
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Serving
;; **

;; @@
(let [dataset (->> data-loaded
                   to-weka
                   (mlf/make-apply-filter
                     :replace-missing-values {}))
      classifier (cl/make-classifier :decision-tree
                                     :random-forest)
      file (io/file "resources/classifier.bin")]
  (print file)
  (clu/serialize-to-file classifier file))
;; @@
;; ->
;;; #object[java.io.File 0xc00fbd resources/classifier.bin]
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>#object[java.io.File 0xc00fbd &quot;resources/classifier.bin&quot;]</span>","value":"#object[java.io.File 0xc00fbd \"resources/classifier.bin\"]"}
;; <=

;; @@
(let [file (io/file "resources/classifier.bin")
      row (->> data-loaded
               to-weka
               (mlf/make-apply-filter
                      :replace-missing-values {}))
      classifier (clu/deserialize-from-file file)]
  (println classifier)
  (cl/classifier-classify classifier row))
;; @@
;; ->
;;; #object[weka.classifiers.trees.RandomForest 0x5ce63f4a Random forest not built yet]
;;; 
;; <-

;; @@

;; @@
