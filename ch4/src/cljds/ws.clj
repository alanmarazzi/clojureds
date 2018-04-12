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
     clojure.pprint/print-table)
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
;;; {"type":"html","content":"<img src=\"data:image/PNG;base64,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qqf4sRF7sjHmduUQfEz58+jxPCDaLNBrKYanjGWff/559jz+xvOYX+/9zbynUn8tb3FYvRlvlT30Sp1x1CT6Fzx//lwwCNCJ2hHosbGP1wQEd/tZzcyrgKXW4ft6z5v11KziHH81wFsB9DjPnz/FUBmLOubXe38z72kF0OMc+sTExLajFdWnQ4gAnahNgB4dz+I10bntIIBeDac4PNwIms16alaVw/XRMayiytGC/I+G7777bldADw0ODr7tVR9/33vvvYbfUaP3VA655w+X73TIvZGiD0H0Jah3moEI0InaAOj//ve/s72+gMry8nI2LzpiBWzy127vFeixRxjPAygB1koHsJ2g2aynWgrg3b59++155QB2BZzRSa6iSo/zmzdvZs+jH0AFpLsBenT4i+WnTp3K/v71r39t+B01ek+lw2GcIqh0irt8+XI2LzoeNuMt3hu95+M7zx8ZyPekJwJ0ojYCeij2BKPTWKWHeIAmeq7nL3HaK9ADyHHYPfaIBwYGMtg2A81mPNXSlStX0i9/+cvs3Hn8z9gjjcvrPvvssy174z/88EMGvTjsHq8L0M3Pz+8a6JVObZXz+5XObju9v9F7KlCPHxjhL2qIHyr5Hu6NvMX3dPXq1eyoROVywTgE7xw6AToREREBOhEREaATERERoBMRERGgExEREaATEREBOhEREQE6ERERAToREREBOhER0XHR/wHFqs/O1RrIiAAAAABJRU5ErkJggg==\"/>","value":"#incanter_gorilla.render.ChartView{:content #object[org.jfree.chart.JFreeChart 0x6d7ad022 \"org.jfree.chart.JFreeChart@6d7ad022\"], :opts nil}"}
;; <=

;; @@
(-> (concat (repeat 127 0)
            (repeat 339 1))
    (s/bootstrap i/sum :size 10000)
    (s/sd))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>9.601632932761861</span>","value":"9.601632932761861"}
;; <=

;; **
;;; ## Binomial Distribution
;; **

;; @@

;; @@
