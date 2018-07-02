;; gorilla-repl.fileformat = 1

;; **
;;; # Chapter 9 - Time Series
;; **

;; @@
(ns ch9.ws
  (:require [gorilla-plot.core :as plot]
            [incanter.charts :as c]
            [incanter.core :as i ]
            [incanter.datasets :as d]
            [incanter.stats :as s]
            [incanter.svg :as svg]
			[succession.core :as sc]
            [clojure.pprint :as pp]
            [huri.plot :as plt]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(->> (d/get-dataset :longley)
     :rows
     (pp/print-table))
;; @@
;; ->
;;; 
;;; |    :y |   :x1 |    :x2 |  :x3 |  :x4 |    :x5 |  :x6 |
;;; |-------+-------+--------+------+------+--------+------|
;;; | 60323 |  83.0 | 234289 | 2356 | 1590 | 107608 | 1947 |
;;; | 61122 |  88.5 | 259426 | 2325 | 1456 | 108632 | 1948 |
;;; | 60171 |  88.2 | 258054 | 3682 | 1616 | 109773 | 1949 |
;;; | 61187 |  89.5 | 284599 | 3351 | 1650 | 110929 | 1950 |
;;; | 63221 |  96.2 | 328975 | 2099 | 3099 | 112075 | 1951 |
;;; | 63639 |  98.1 | 346999 | 1932 | 3594 | 113270 | 1952 |
;;; | 64989 |  99.0 | 365385 | 1870 | 3547 | 115094 | 1953 |
;;; | 63761 | 100.0 | 363112 | 3578 | 3350 | 116219 | 1954 |
;;; | 66019 | 101.2 | 397469 | 2904 | 3048 | 117388 | 1955 |
;;; | 67857 | 104.6 | 419180 | 2822 | 2857 | 118734 | 1956 |
;;; | 68169 | 108.4 | 442769 | 2936 | 2798 | 120445 | 1957 |
;;; | 66513 | 110.8 | 444546 | 4681 | 2637 | 121950 | 1958 |
;;; | 68655 | 112.6 | 482704 | 3813 | 2552 | 123366 | 1959 |
;;; | 69564 | 114.2 | 502601 | 3931 | 2514 | 125368 | 1960 |
;;; | 69331 | 115.7 | 518173 | 4806 | 2572 | 127852 | 1961 |
;;; | 70551 | 116.9 | 554894 | 4007 | 2827 | 130081 | 1962 |
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; The **Longley** dataset contains data about 7 economic variables measured in the US between 1947 and 1962. We care about the last 3 cols: 
;;; 
;;; - **x4**: size of armed forces
;;; - **x5**: population 14+
;;; - **x6**: the year
;;; 
;;; To begin we see how population grows during time
;; **

;; @@
(let [data (:rows (d/get-dataset :longley))]
  (as-> data d
        (map (juxt :x6 :x5) d)
        (plot/list-plot d :plot-size 600)))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"top":10,"left":55,"bottom":40,"right":10},"data":[{"name":"bd597dd6-556f-476b-81d8-63accb47fc20","values":[{"x":1947,"y":107608},{"x":1948,"y":108632},{"x":1949,"y":109773},{"x":1950,"y":110929},{"x":1951,"y":112075},{"x":1952,"y":113270},{"x":1953,"y":115094},{"x":1954,"y":116219},{"x":1955,"y":117388},{"x":1956,"y":118734},{"x":1957,"y":120445},{"x":1958,"y":121950},{"x":1959,"y":123366},{"x":1960,"y":125368},{"x":1961,"y":127852},{"x":1962,"y":130081}]}],"marks":[{"type":"symbol","from":{"data":"bd597dd6-556f-476b-81d8-63accb47fc20"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"fill":{"value":"steelblue"},"fillOpacity":{"value":1}},"update":{"shape":"circle","size":{"value":70},"stroke":{"value":"transparent"}},"hover":{"size":{"value":210},"stroke":{"value":"white"}}}}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"bd597dd6-556f-476b-81d8-63accb47fc20","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"bd597dd6-556f-476b-81d8-63accb47fc20","field":"data.y"}}],"axes":[{"type":"x","scale":"x"},{"type":"y","scale":"y"}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:top 10, :left 55, :bottom 40, :right 10}, :data [{:name \"bd597dd6-556f-476b-81d8-63accb47fc20\", :values ({:x 1947, :y 107608} {:x 1948, :y 108632} {:x 1949, :y 109773} {:x 1950, :y 110929} {:x 1951, :y 112075} {:x 1952, :y 113270} {:x 1953, :y 115094} {:x 1954, :y 116219} {:x 1955, :y 117388} {:x 1956, :y 118734} {:x 1957, :y 120445} {:x 1958, :y 121950} {:x 1959, :y 123366} {:x 1960, :y 125368} {:x 1961, :y 127852} {:x 1962, :y 130081})}], :marks [{:type \"symbol\", :from {:data \"bd597dd6-556f-476b-81d8-63accb47fc20\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 1}}, :update {:shape \"circle\", :size {:value 70}, :stroke {:value \"transparent\"}}, :hover {:size {:value 210}, :stroke {:value \"white\"}}}}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"bd597dd6-556f-476b-81d8-63accb47fc20\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"bd597dd6-556f-476b-81d8-63accb47fc20\", :field \"data.y\"}}], :axes [{:type \"x\", :scale \"x\"} {:type \"y\", :scale \"y\"}]}}"}
;; <=

;; @@
(let [data (d/get-dataset :longley)
      rows (:rows data)
      model (s/linear-model (i/$ :x5 data)
                            (i/$ :x6 data))]
  (println (:coefs model)))
;; @@
;; ->
;;; [-2720975.6308555603 1452.2382352799177]
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@

;; @@
