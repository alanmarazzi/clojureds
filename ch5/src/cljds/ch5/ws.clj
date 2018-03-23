;; gorilla-repl.fileformat = 1

;; **
;;; # Chapter 5 - Big Data
;; **

;; @@
(ns cljds.ch5.ws
  (:require [abracad.avro :as avro]
            [clojure.core.reducers :as r]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [incanter.charts :as c]
            [incanter.core :as i]
            [iota]
            ;[parkour.conf :as conf]
            ;[parkour.io.text :as text]
            [tesser.core :as t]
            ;[tesser.hadoop :as h]
            [tesser.math :as m]
			[incanter.svg :as svg])
  (:use [clojure.repl :only [doc source]]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Data Inspection
;; **

;; @@
(-> (slurp "data/soi.csv")
    (str/split #"\n")
    (first))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;STATEFIPS,STATE,zipcode,AGI_STUB,N1,MARS1,MARS2,MARS4,PREP,N2,NUMDEP,A00100,N00200,A00200,N00300,A00300,N00600,A00600,N00650,A00650,N00900,A00900,SCHF,N01000,A01000,N01400,A01400,N01700,A01700,N02300,A02300,N02500,A02500,N03300,A03300,N00101,A00101,N04470,A04470,N18425,A18425,N18450,A18450,N18500,A18500,N18300,A18300,N19300,A19300,N19700,A19700,N04800,A04800,N07100,A07100,N07220,A07220,N07180,A07180,N07260,A07260,N59660,A59660,N59720,A59720,N11070,A11070,N09600,A09600,N06500,A06500,N10300,A10300,N11901,A11901,N11902,A11902&quot;</span>","value":"\"STATEFIPS,STATE,zipcode,AGI_STUB,N1,MARS1,MARS2,MARS4,PREP,N2,NUMDEP,A00100,N00200,A00200,N00300,A00300,N00600,A00600,N00650,A00650,N00900,A00900,SCHF,N01000,A01000,N01400,A01400,N01700,A01700,N02300,A02300,N02500,A02500,N03300,A03300,N00101,A00101,N04470,A04470,N18425,A18425,N18450,A18450,N18500,A18500,N18300,A18300,N19300,A19300,N19700,A19700,N04800,A04800,N07100,A07100,N07220,A07220,N07180,A07180,N07260,A07260,N59660,A59660,N59720,A59720,N11070,A11070,N09600,A09600,N06500,A06500,N10300,A10300,N11901,A11901,N11902,A11902\""}
;; <=

;; **
;;; For instance if we want to see only the first line, it doesn't make much sense to load the whole dataset (= 100 MB) in memory. We can take advantage of **Clojure**'s lazy features
;; **

;; @@
(-> (io/reader "data/soi.csv")
    (line-seq)
    (first))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;STATEFIPS,STATE,zipcode,AGI_STUB,N1,MARS1,MARS2,MARS4,PREP,N2,NUMDEP,A00100,N00200,A00200,N00300,A00300,N00600,A00600,N00650,A00650,N00900,A00900,SCHF,N01000,A01000,N01400,A01400,N01700,A01700,N02300,A02300,N02500,A02500,N03300,A03300,N00101,A00101,N04470,A04470,N18425,A18425,N18450,A18450,N18500,A18500,N18300,A18300,N19300,A19300,N19700,A19700,N04800,A04800,N07100,A07100,N07220,A07220,N07180,A07180,N07260,A07260,N59660,A59660,N59720,A59720,N11070,A11070,N09600,A09600,N06500,A06500,N10300,A10300,N11901,A11901,N11902,A11902&quot;</span>","value":"\"STATEFIPS,STATE,zipcode,AGI_STUB,N1,MARS1,MARS2,MARS4,PREP,N2,NUMDEP,A00100,N00200,A00200,N00300,A00300,N00600,A00600,N00650,A00650,N00900,A00900,SCHF,N01000,A01000,N01400,A01400,N01700,A01700,N02300,A02300,N02500,A02500,N03300,A03300,N00101,A00101,N04470,A04470,N18425,A18425,N18450,A18450,N18500,A18500,N18300,A18300,N19300,A19300,N19700,A19700,N04800,A04800,N07100,A07100,N07220,A07220,N07180,A07180,N07260,A07260,N59660,A59660,N59720,A59720,N11070,A11070,N09600,A09600,N06500,A06500,N10300,A10300,N11901,A11901,N11902,A11902\""}
;; <=

;; **
;;; So we have 77 cols, but how many rows do we have?
;; **

;; @@
(time
  (-> (io/reader "data/soi.csv")
      (line-seq)
      (count)))
;; @@
;; ->
;;; &quot;Elapsed time: 725.228 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>166905</span>","value":"166905"}
;; <=

;; **
;;; ## Reducers
;;; 
;;; With **reducers** we can easily parallelize our code! In the homonym library we find the `fold` function which is a parallel implementation of reduce.
;;; 
;;; For instance, to count the rows of our file in a parallel fashion:
;; **

;; @@
(time 
  (->> (io/reader "data/soi.csv")
       (line-seq)
       (r/fold + (fn [i x]
                   (inc i)))))
;; @@
;; ->
;;; &quot;Elapsed time: 368.439 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-long'>166905</span>","value":"166905"}
;; <=

;; @@
(doc r/fold)
;; @@
;; ->
;;; -------------------------
;;; clojure.core.reducers/fold
;;; ([reducef coll] [combinef reducef coll] [n combinef reducef coll])
;;;   Reduces a collection using a (potentially parallel) reduce-combine
;;;   strategy. The collection is partitioned into groups of approximately
;;;   n (default 512), each of which is reduced with reducef (with a seed
;;;   value obtained by calling (combinef) with no arguments). The results
;;;   of these reductions are then reduced with combinef (default
;;;   reducef). combinef must be associative, and, when called with no
;;;   arguments, (combinef) must produce its identity element. These
;;;   operations may be performed in parallel, but the results will
;;;   preserve order.
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; In the previous case we gave `+` as a **combiner function** and a function where we increment a counter for every element passing through the function itself.
;;; 
;;; The issue is that by calling `fold` on a lazy sequence, we have to load it in memory, and then chunk the sequence into groups. By using **iota** we can improve a bit the situation
;; **

;; @@
(time
  (->> (iota/seq "data/soi.csv")
       (r/fold + (fn [i x]
                   (inc i)))))
;; @@
;; ->
;;; &quot;Elapsed time: 360.325 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-long'>166905</span>","value":"166905"}
;; <=

;; **
;;; ## Creating a Reducers Pipeline
;;; 
;;; Let's make functions that load the *csv* data as Clojure data structures
;; **

;; @@
(defn parse-double
  [x]
  (Double/parseDouble x))

(defn parse-line
  [line]
  (let [[text-fields double-fields] (->> (str/split line #",")
                                         (split-at 2))]
    (concat text-fields
            (map parse-double double-fields))))

(defn format-record [column-names line]
  (zipmap column-names line))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/format-record</span>","value":"#'cljds.ch5.ws/format-record"}
;; <=

;; @@
(time
  (->> (iota/seq "data/soi.csv")
       (r/drop 1)
       (r/map parse-line)
       (r/take 1)
       (into [])))
;; @@
;; ->
;;; &quot;Elapsed time: 11.785 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;01&quot;</span>","value":"\"01\""},{"type":"html","content":"<span class='clj-string'>&quot;AL&quot;</span>","value":"\"AL\""},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>1.0</span>","value":"1.0"},{"type":"html","content":"<span class='clj-double'>889920.0</span>","value":"889920.0"},{"type":"html","content":"<span class='clj-double'>490850.0</span>","value":"490850.0"},{"type":"html","content":"<span class='clj-double'>129070.0</span>","value":"129070.0"},{"type":"html","content":"<span class='clj-double'>256900.0</span>","value":"256900.0"},{"type":"html","content":"<span class='clj-double'>526740.0</span>","value":"526740.0"},{"type":"html","content":"<span class='clj-double'>1505430.0</span>","value":"1505430.0"},{"type":"html","content":"<span class='clj-double'>598680.0</span>","value":"598680.0"},{"type":"html","content":"<span class='clj-double'>1.1517112E7</span>","value":"1.1517112E7"},{"type":"html","content":"<span class='clj-double'>712630.0</span>","value":"712630.0"},{"type":"html","content":"<span class='clj-double'>9014595.0</span>","value":"9014595.0"},{"type":"html","content":"<span class='clj-double'>116620.0</span>","value":"116620.0"},{"type":"html","content":"<span class='clj-double'>101108.0</span>","value":"101108.0"},{"type":"html","content":"<span class='clj-double'>49750.0</span>","value":"49750.0"},{"type":"html","content":"<span class='clj-double'>92178.0</span>","value":"92178.0"},{"type":"html","content":"<span class='clj-double'>43200.0</span>","value":"43200.0"},{"type":"html","content":"<span class='clj-double'>59907.0</span>","value":"59907.0"},{"type":"html","content":"<span class='clj-double'>150240.0</span>","value":"150240.0"},{"type":"html","content":"<span class='clj-double'>873349.0</span>","value":"873349.0"},{"type":"html","content":"<span class='clj-double'>9120.0</span>","value":"9120.0"},{"type":"html","content":"<span class='clj-double'>35010.0</span>","value":"35010.0"},{"type":"html","content":"<span class='clj-double'>-3916.0</span>","value":"-3916.0"},{"type":"html","content":"<span class='clj-double'>38910.0</span>","value":"38910.0"},{"type":"html","content":"<span class='clj-double'>221795.0</span>","value":"221795.0"},{"type":"html","content":"<span class='clj-double'>112470.0</span>","value":"112470.0"},{"type":"html","content":"<span class='clj-double'>1072785.0</span>","value":"1072785.0"},{"type":"html","content":"<span class='clj-double'>65230.0</span>","value":"65230.0"},{"type":"html","content":"<span class='clj-double'>265804.0</span>","value":"265804.0"},{"type":"html","content":"<span class='clj-double'>34100.0</span>","value":"34100.0"},{"type":"html","content":"<span class='clj-double'>59389.0</span>","value":"59389.0"},{"type":"html","content":"<span class='clj-double'>260.0</span>","value":"260.0"},{"type":"html","content":"<span class='clj-double'>843.0</span>","value":"843.0"},{"type":"html","content":"<span class='clj-double'>62190.0</span>","value":"62190.0"},{"type":"html","content":"<span class='clj-double'>968541.0</span>","value":"968541.0"},{"type":"html","content":"<span class='clj-double'>62120.0</span>","value":"62120.0"},{"type":"html","content":"<span class='clj-double'>850798.0</span>","value":"850798.0"},{"type":"html","content":"<span class='clj-double'>24840.0</span>","value":"24840.0"},{"type":"html","content":"<span class='clj-double'>23577.0</span>","value":"23577.0"},{"type":"html","content":"<span class='clj-double'>29040.0</span>","value":"29040.0"},{"type":"html","content":"<span class='clj-double'>23668.0</span>","value":"23668.0"},{"type":"html","content":"<span class='clj-double'>33360.0</span>","value":"33360.0"},{"type":"html","content":"<span class='clj-double'>27495.0</span>","value":"27495.0"},{"type":"html","content":"<span class='clj-double'>60070.0</span>","value":"60070.0"},{"type":"html","content":"<span class='clj-double'>90908.0</span>","value":"90908.0"},{"type":"html","content":"<span class='clj-double'>31110.0</span>","value":"31110.0"},{"type":"html","content":"<span class='clj-double'>181519.0</span>","value":"181519.0"},{"type":"html","content":"<span class='clj-double'>46490.0</span>","value":"46490.0"},{"type":"html","content":"<span class='clj-double'>121095.0</span>","value":"121095.0"},{"type":"html","content":"<span class='clj-double'>356200.0</span>","value":"356200.0"},{"type":"html","content":"<span class='clj-double'>1962972.0</span>","value":"1962972.0"},{"type":"html","content":"<span class='clj-double'>112340.0</span>","value":"112340.0"},{"type":"html","content":"<span class='clj-double'>38443.0</span>","value":"38443.0"},{"type":"html","content":"<span class='clj-double'>39440.0</span>","value":"39440.0"},{"type":"html","content":"<span class='clj-double'>12168.0</span>","value":"12168.0"},{"type":"html","content":"<span class='clj-double'>9920.0</span>","value":"9920.0"},{"type":"html","content":"<span class='clj-double'>3393.0</span>","value":"3393.0"},{"type":"html","content":"<span class='clj-double'>1680.0</span>","value":"1680.0"},{"type":"html","content":"<span class='clj-double'>408.0</span>","value":"408.0"},{"type":"html","content":"<span class='clj-double'>406300.0</span>","value":"406300.0"},{"type":"html","content":"<span class='clj-double'>1178852.0</span>","value":"1178852.0"},{"type":"html","content":"<span class='clj-double'>379060.0</span>","value":"379060.0"},{"type":"html","content":"<span class='clj-double'>1071400.0</span>","value":"1071400.0"},{"type":"html","content":"<span class='clj-double'>266230.0</span>","value":"266230.0"},{"type":"html","content":"<span class='clj-double'>335781.0</span>","value":"335781.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>269140.0</span>","value":"269140.0"},{"type":"html","content":"<span class='clj-double'>168057.0</span>","value":"168057.0"},{"type":"html","content":"<span class='clj-double'>387720.0</span>","value":"387720.0"},{"type":"html","content":"<span class='clj-double'>314297.0</span>","value":"314297.0"},{"type":"html","content":"<span class='clj-double'>59310.0</span>","value":"59310.0"},{"type":"html","content":"<span class='clj-double'>40782.0</span>","value":"40782.0"},{"type":"html","content":"<span class='clj-double'>785950.0</span>","value":"785950.0"},{"type":"html","content":"<span class='clj-double'>2040191.0</span>","value":"2040191.0"}],"value":"(\"01\" \"AL\" 0.0 1.0 889920.0 490850.0 129070.0 256900.0 526740.0 1505430.0 598680.0 1.1517112E7 712630.0 9014595.0 116620.0 101108.0 49750.0 92178.0 43200.0 59907.0 150240.0 873349.0 9120.0 35010.0 -3916.0 38910.0 221795.0 112470.0 1072785.0 65230.0 265804.0 34100.0 59389.0 260.0 843.0 62190.0 968541.0 62120.0 850798.0 24840.0 23577.0 29040.0 23668.0 33360.0 27495.0 60070.0 90908.0 31110.0 181519.0 46490.0 121095.0 356200.0 1962972.0 112340.0 38443.0 39440.0 12168.0 9920.0 3393.0 1680.0 408.0 406300.0 1178852.0 379060.0 1071400.0 266230.0 335781.0 0.0 0.0 269140.0 168057.0 387720.0 314297.0 59310.0 40782.0 785950.0 2040191.0)"}],"value":"[(\"01\" \"AL\" 0.0 1.0 889920.0 490850.0 129070.0 256900.0 526740.0 1505430.0 598680.0 1.1517112E7 712630.0 9014595.0 116620.0 101108.0 49750.0 92178.0 43200.0 59907.0 150240.0 873349.0 9120.0 35010.0 -3916.0 38910.0 221795.0 112470.0 1072785.0 65230.0 265804.0 34100.0 59389.0 260.0 843.0 62190.0 968541.0 62120.0 850798.0 24840.0 23577.0 29040.0 23668.0 33360.0 27495.0 60070.0 90908.0 31110.0 181519.0 46490.0 121095.0 356200.0 1962972.0 112340.0 38443.0 39440.0 12168.0 9920.0 3393.0 1680.0 408.0 406300.0 1178852.0 379060.0 1071400.0 266230.0 335781.0 0.0 0.0 269140.0 168057.0 387720.0 314297.0 59310.0 40782.0 785950.0 2040191.0)]"}
;; <=

;; @@
(defn parse-columns
  [line]
  (->> (str/split line #",")
       (map keyword)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/parse-columns</span>","value":"#'cljds.ch5.ws/parse-columns"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")
      column-names (parse-columns (first data))]
  (->> (r/drop 1 data)
       (r/map parse-line)
       (r/map (fn [fields]
                (zipmap column-names fields)))
       (r/take 1)
       (into [])))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N2</span>","value":":N2"},{"type":"html","content":"<span class='clj-double'>1505430.0</span>","value":"1505430.0"}],"value":"[:N2 1505430.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A19300</span>","value":":A19300"},{"type":"html","content":"<span class='clj-double'>181519.0</span>","value":"181519.0"}],"value":"[:A19300 181519.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS4</span>","value":":MARS4"},{"type":"html","content":"<span class='clj-double'>256900.0</span>","value":"256900.0"}],"value":"[:MARS4 256900.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07220</span>","value":":N07220"},{"type":"html","content":"<span class='clj-double'>39440.0</span>","value":"39440.0"}],"value":"[:N07220 39440.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N19700</span>","value":":N19700"},{"type":"html","content":"<span class='clj-double'>46490.0</span>","value":"46490.0"}],"value":"[:N19700 46490.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18500</span>","value":":A18500"},{"type":"html","content":"<span class='clj-double'>27495.0</span>","value":"27495.0"}],"value":"[:A18500 27495.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A01400</span>","value":":A01400"},{"type":"html","content":"<span class='clj-double'>221795.0</span>","value":"221795.0"}],"value":"[:A01400 221795.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N01700</span>","value":":N01700"},{"type":"html","content":"<span class='clj-double'>112470.0</span>","value":"112470.0"}],"value":"[:N01700 112470.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18500</span>","value":":N18500"},{"type":"html","content":"<span class='clj-double'>33360.0</span>","value":"33360.0"}],"value":"[:N18500 33360.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A01000</span>","value":":A01000"},{"type":"html","content":"<span class='clj-double'>-3916.0</span>","value":"-3916.0"}],"value":"[:A01000 -3916.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18300</span>","value":":N18300"},{"type":"html","content":"<span class='clj-double'>60070.0</span>","value":"60070.0"}],"value":"[:N18300 60070.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07220</span>","value":":A07220"},{"type":"html","content":"<span class='clj-double'>12168.0</span>","value":"12168.0"}],"value":"[:A07220 12168.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00100</span>","value":":A00100"},{"type":"html","content":"<span class='clj-double'>1.1517112E7</span>","value":"1.1517112E7"}],"value":"[:A00100 1.1517112E7]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07100</span>","value":":N07100"},{"type":"html","content":"<span class='clj-double'>112340.0</span>","value":"112340.0"}],"value":"[:N07100 112340.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:STATE</span>","value":":STATE"},{"type":"html","content":"<span class='clj-string'>&quot;AL&quot;</span>","value":"\"AL\""}],"value":"[:STATE \"AL\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A11070</span>","value":":A11070"},{"type":"html","content":"<span class='clj-double'>335781.0</span>","value":"335781.0"}],"value":"[:A11070 335781.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A59660</span>","value":":A59660"},{"type":"html","content":"<span class='clj-double'>1178852.0</span>","value":"1178852.0"}],"value":"[:A59660 1178852.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00200</span>","value":":N00200"},{"type":"html","content":"<span class='clj-double'>712630.0</span>","value":"712630.0"}],"value":"[:N00200 712630.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00650</span>","value":":A00650"},{"type":"html","content":"<span class='clj-double'>59907.0</span>","value":"59907.0"}],"value":"[:A00650 59907.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00600</span>","value":":N00600"},{"type":"html","content":"<span class='clj-double'>49750.0</span>","value":"49750.0"}],"value":"[:N00600 49750.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00101</span>","value":":N00101"},{"type":"html","content":"<span class='clj-double'>62190.0</span>","value":"62190.0"}],"value":"[:N00101 62190.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:AGI_STUB</span>","value":":AGI_STUB"},{"type":"html","content":"<span class='clj-double'>1.0</span>","value":"1.0"}],"value":"[:AGI_STUB 1.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N04800</span>","value":":N04800"},{"type":"html","content":"<span class='clj-double'>356200.0</span>","value":"356200.0"}],"value":"[:N04800 356200.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A11901</span>","value":":A11901"},{"type":"html","content":"<span class='clj-double'>40782.0</span>","value":"40782.0"}],"value":"[:A11901 40782.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00600</span>","value":":A00600"},{"type":"html","content":"<span class='clj-double'>92178.0</span>","value":"92178.0"}],"value":"[:A00600 92178.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A02500</span>","value":":A02500"},{"type":"html","content":"<span class='clj-double'>59389.0</span>","value":"59389.0"}],"value":"[:A02500 59389.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N11902</span>","value":":N11902"},{"type":"html","content":"<span class='clj-double'>785950.0</span>","value":"785950.0"}],"value":"[:N11902 785950.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N10300</span>","value":":N10300"},{"type":"html","content":"<span class='clj-double'>387720.0</span>","value":"387720.0"}],"value":"[:N10300 387720.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N01400</span>","value":":N01400"},{"type":"html","content":"<span class='clj-double'>38910.0</span>","value":"38910.0"}],"value":"[:N01400 38910.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:zipcode</span>","value":":zipcode"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:zipcode 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07100</span>","value":":A07100"},{"type":"html","content":"<span class='clj-double'>38443.0</span>","value":"38443.0"}],"value":"[:A07100 38443.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:STATEFIPS</span>","value":":STATEFIPS"},{"type":"html","content":"<span class='clj-string'>&quot;01&quot;</span>","value":"\"01\""}],"value":"[:STATEFIPS \"01\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18450</span>","value":":A18450"},{"type":"html","content":"<span class='clj-double'>23668.0</span>","value":"23668.0"}],"value":"[:A18450 23668.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A01700</span>","value":":A01700"},{"type":"html","content":"<span class='clj-double'>1072785.0</span>","value":"1072785.0"}],"value":"[:A01700 1072785.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00900</span>","value":":A00900"},{"type":"html","content":"<span class='clj-double'>873349.0</span>","value":"873349.0"}],"value":"[:A00900 873349.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N1</span>","value":":N1"},{"type":"html","content":"<span class='clj-double'>889920.0</span>","value":"889920.0"}],"value":"[:N1 889920.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N02500</span>","value":":N02500"},{"type":"html","content":"<span class='clj-double'>34100.0</span>","value":"34100.0"}],"value":"[:N02500 34100.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A59720</span>","value":":A59720"},{"type":"html","content":"<span class='clj-double'>1071400.0</span>","value":"1071400.0"}],"value":"[:A59720 1071400.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N09600</span>","value":":N09600"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:N09600 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00101</span>","value":":A00101"},{"type":"html","content":"<span class='clj-double'>968541.0</span>","value":"968541.0"}],"value":"[:A00101 968541.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07260</span>","value":":N07260"},{"type":"html","content":"<span class='clj-double'>1680.0</span>","value":"1680.0"}],"value":"[:N07260 1680.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A09600</span>","value":":A09600"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:A09600 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A06500</span>","value":":A06500"},{"type":"html","content":"<span class='clj-double'>168057.0</span>","value":"168057.0"}],"value":"[:A06500 168057.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07180</span>","value":":A07180"},{"type":"html","content":"<span class='clj-double'>3393.0</span>","value":"3393.0"}],"value":"[:A07180 3393.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS2</span>","value":":MARS2"},{"type":"html","content":"<span class='clj-double'>129070.0</span>","value":"129070.0"}],"value":"[:MARS2 129070.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N59660</span>","value":":N59660"},{"type":"html","content":"<span class='clj-double'>406300.0</span>","value":"406300.0"}],"value":"[:N59660 406300.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A11902</span>","value":":A11902"},{"type":"html","content":"<span class='clj-double'>2040191.0</span>","value":"2040191.0"}],"value":"[:A11902 2040191.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N01000</span>","value":":N01000"},{"type":"html","content":"<span class='clj-double'>35010.0</span>","value":"35010.0"}],"value":"[:N01000 35010.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07180</span>","value":":N07180"},{"type":"html","content":"<span class='clj-double'>9920.0</span>","value":"9920.0"}],"value":"[:N07180 9920.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A04470</span>","value":":A04470"},{"type":"html","content":"<span class='clj-double'>850798.0</span>","value":"850798.0"}],"value":"[:A04470 850798.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N04470</span>","value":":N04470"},{"type":"html","content":"<span class='clj-double'>62120.0</span>","value":"62120.0"}],"value":"[:N04470 62120.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00900</span>","value":":N00900"},{"type":"html","content":"<span class='clj-double'>150240.0</span>","value":"150240.0"}],"value":"[:N00900 150240.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N19300</span>","value":":N19300"},{"type":"html","content":"<span class='clj-double'>31110.0</span>","value":"31110.0"}],"value":"[:N19300 31110.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18425</span>","value":":N18425"},{"type":"html","content":"<span class='clj-double'>24840.0</span>","value":"24840.0"}],"value":"[:N18425 24840.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS1</span>","value":":MARS1"},{"type":"html","content":"<span class='clj-double'>490850.0</span>","value":"490850.0"}],"value":"[:MARS1 490850.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A03300</span>","value":":A03300"},{"type":"html","content":"<span class='clj-double'>843.0</span>","value":"843.0"}],"value":"[:A03300 843.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A04800</span>","value":":A04800"},{"type":"html","content":"<span class='clj-double'>1962972.0</span>","value":"1962972.0"}],"value":"[:A04800 1962972.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N02300</span>","value":":N02300"},{"type":"html","content":"<span class='clj-double'>65230.0</span>","value":"65230.0"}],"value":"[:N02300 65230.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A10300</span>","value":":A10300"},{"type":"html","content":"<span class='clj-double'>314297.0</span>","value":"314297.0"}],"value":"[:A10300 314297.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00300</span>","value":":A00300"},{"type":"html","content":"<span class='clj-double'>101108.0</span>","value":"101108.0"}],"value":"[:A00300 101108.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:SCHF</span>","value":":SCHF"},{"type":"html","content":"<span class='clj-double'>9120.0</span>","value":"9120.0"}],"value":"[:SCHF 9120.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N03300</span>","value":":N03300"},{"type":"html","content":"<span class='clj-double'>260.0</span>","value":"260.0"}],"value":"[:N03300 260.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18425</span>","value":":A18425"},{"type":"html","content":"<span class='clj-double'>23577.0</span>","value":"23577.0"}],"value":"[:A18425 23577.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18450</span>","value":":N18450"},{"type":"html","content":"<span class='clj-double'>29040.0</span>","value":"29040.0"}],"value":"[:N18450 29040.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18300</span>","value":":A18300"},{"type":"html","content":"<span class='clj-double'>90908.0</span>","value":"90908.0"}],"value":"[:A18300 90908.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00200</span>","value":":A00200"},{"type":"html","content":"<span class='clj-double'>9014595.0</span>","value":"9014595.0"}],"value":"[:A00200 9014595.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07260</span>","value":":A07260"},{"type":"html","content":"<span class='clj-double'>408.0</span>","value":"408.0"}],"value":"[:A07260 408.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A19700</span>","value":":A19700"},{"type":"html","content":"<span class='clj-double'>121095.0</span>","value":"121095.0"}],"value":"[:A19700 121095.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00650</span>","value":":N00650"},{"type":"html","content":"<span class='clj-double'>43200.0</span>","value":"43200.0"}],"value":"[:N00650 43200.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N11070</span>","value":":N11070"},{"type":"html","content":"<span class='clj-double'>266230.0</span>","value":"266230.0"}],"value":"[:N11070 266230.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N06500</span>","value":":N06500"},{"type":"html","content":"<span class='clj-double'>269140.0</span>","value":"269140.0"}],"value":"[:N06500 269140.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:PREP</span>","value":":PREP"},{"type":"html","content":"<span class='clj-double'>526740.0</span>","value":"526740.0"}],"value":"[:PREP 526740.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A02300</span>","value":":A02300"},{"type":"html","content":"<span class='clj-double'>265804.0</span>","value":"265804.0"}],"value":"[:A02300 265804.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N59720</span>","value":":N59720"},{"type":"html","content":"<span class='clj-double'>379060.0</span>","value":"379060.0"}],"value":"[:N59720 379060.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00300</span>","value":":N00300"},{"type":"html","content":"<span class='clj-double'>116620.0</span>","value":"116620.0"}],"value":"[:N00300 116620.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:NUMDEP</span>","value":":NUMDEP"},{"type":"html","content":"<span class='clj-double'>598680.0</span>","value":"598680.0"}],"value":"[:NUMDEP 598680.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N11901</span>","value":":N11901"},{"type":"html","content":"<span class='clj-double'>59310.0</span>","value":"59310.0"}],"value":"[:N11901 59310.0]"}],"value":"{:N2 1505430.0, :A19300 181519.0, :MARS4 256900.0, :N07220 39440.0, :N19700 46490.0, :A18500 27495.0, :A01400 221795.0, :N01700 112470.0, :N18500 33360.0, :A01000 -3916.0, :N18300 60070.0, :A07220 12168.0, :A00100 1.1517112E7, :N07100 112340.0, :STATE \"AL\", :A11070 335781.0, :A59660 1178852.0, :N00200 712630.0, :A00650 59907.0, :N00600 49750.0, :N00101 62190.0, :AGI_STUB 1.0, :N04800 356200.0, :A11901 40782.0, :A00600 92178.0, :A02500 59389.0, :N11902 785950.0, :N10300 387720.0, :N01400 38910.0, :zipcode 0.0, :A07100 38443.0, :STATEFIPS \"01\", :A18450 23668.0, :A01700 1072785.0, :A00900 873349.0, :N1 889920.0, :N02500 34100.0, :A59720 1071400.0, :N09600 0.0, :A00101 968541.0, :N07260 1680.0, :A09600 0.0, :A06500 168057.0, :A07180 3393.0, :MARS2 129070.0, :N59660 406300.0, :A11902 2040191.0, :N01000 35010.0, :N07180 9920.0, :A04470 850798.0, :N04470 62120.0, :N00900 150240.0, :N19300 31110.0, :N18425 24840.0, :MARS1 490850.0, :A03300 843.0, :A04800 1962972.0, :N02300 65230.0, :A10300 314297.0, :A00300 101108.0, :SCHF 9120.0, :N03300 260.0, :A18425 23577.0, :N18450 29040.0, :A18300 90908.0, :A00200 9014595.0, :A07260 408.0, :A19700 121095.0, :N00650 43200.0, :N11070 266230.0, :N06500 269140.0, :PREP 526740.0, :A02300 265804.0, :N59720 379060.0, :N00300 116620.0, :NUMDEP 598680.0, :N11901 59310.0}"}],"value":"[{:N2 1505430.0, :A19300 181519.0, :MARS4 256900.0, :N07220 39440.0, :N19700 46490.0, :A18500 27495.0, :A01400 221795.0, :N01700 112470.0, :N18500 33360.0, :A01000 -3916.0, :N18300 60070.0, :A07220 12168.0, :A00100 1.1517112E7, :N07100 112340.0, :STATE \"AL\", :A11070 335781.0, :A59660 1178852.0, :N00200 712630.0, :A00650 59907.0, :N00600 49750.0, :N00101 62190.0, :AGI_STUB 1.0, :N04800 356200.0, :A11901 40782.0, :A00600 92178.0, :A02500 59389.0, :N11902 785950.0, :N10300 387720.0, :N01400 38910.0, :zipcode 0.0, :A07100 38443.0, :STATEFIPS \"01\", :A18450 23668.0, :A01700 1072785.0, :A00900 873349.0, :N1 889920.0, :N02500 34100.0, :A59720 1071400.0, :N09600 0.0, :A00101 968541.0, :N07260 1680.0, :A09600 0.0, :A06500 168057.0, :A07180 3393.0, :MARS2 129070.0, :N59660 406300.0, :A11902 2040191.0, :N01000 35010.0, :N07180 9920.0, :A04470 850798.0, :N04470 62120.0, :N00900 150240.0, :N19300 31110.0, :N18425 24840.0, :MARS1 490850.0, :A03300 843.0, :A04800 1962972.0, :N02300 65230.0, :A10300 314297.0, :A00300 101108.0, :SCHF 9120.0, :N03300 260.0, :A18425 23577.0, :N18450 29040.0, :A18300 90908.0, :A00200 9014595.0, :A07260 408.0, :A19700 121095.0, :N00650 43200.0, :N11070 266230.0, :N06500 269140.0, :PREP 526740.0, :A02300 265804.0, :N59720 379060.0, :N00300 116620.0, :NUMDEP 598680.0, :N11901 59310.0}]"}
;; <=

;; **
;;; Since the calls to `r/map`, `r/drop` and `r/take` are composed into a **reduction** that will pass only one time over the data, we can complicate stuff as much as we want.
;;; 
;;; Let's say we want to filter out ZIP codes
;; **

;; @@
(let [data (iota/seq "data/soi.csv")
      column-names (parse-columns (first data))]
  (->> (r/drop 1 data)
       (r/map parse-line)
       (r/map (fn [fields]
                (zipmap column-names fields)))
       (r/remove (fn [record]
                   (zero? (:zipcode record))))
       (r/take 1)
       (into [])))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N2</span>","value":":N2"},{"type":"html","content":"<span class='clj-double'>2390.0</span>","value":"2390.0"}],"value":"[:N2 2390.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A19300</span>","value":":A19300"},{"type":"html","content":"<span class='clj-double'>635.0</span>","value":"635.0"}],"value":"[:A19300 635.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS4</span>","value":":MARS4"},{"type":"html","content":"<span class='clj-double'>300.0</span>","value":"300.0"}],"value":"[:MARS4 300.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07220</span>","value":":N07220"},{"type":"html","content":"<span class='clj-double'>60.0</span>","value":"60.0"}],"value":"[:N07220 60.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N19700</span>","value":":N19700"},{"type":"html","content":"<span class='clj-double'>130.0</span>","value":"130.0"}],"value":"[:N19700 130.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18500</span>","value":":A18500"},{"type":"html","content":"<span class='clj-double'>59.0</span>","value":"59.0"}],"value":"[:A18500 59.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A01400</span>","value":":A01400"},{"type":"html","content":"<span class='clj-double'>730.0</span>","value":"730.0"}],"value":"[:A01400 730.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N01700</span>","value":":N01700"},{"type":"html","content":"<span class='clj-double'>240.0</span>","value":"240.0"}],"value":"[:N01700 240.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18500</span>","value":":N18500"},{"type":"html","content":"<span class='clj-double'>110.0</span>","value":"110.0"}],"value":"[:N18500 110.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A01000</span>","value":":A01000"},{"type":"html","content":"<span class='clj-double'>-59.0</span>","value":"-59.0"}],"value":"[:A01000 -59.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18300</span>","value":":N18300"},{"type":"html","content":"<span class='clj-double'>160.0</span>","value":"160.0"}],"value":"[:N18300 160.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07220</span>","value":":A07220"},{"type":"html","content":"<span class='clj-double'>19.0</span>","value":"19.0"}],"value":"[:A07220 19.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00100</span>","value":":A00100"},{"type":"html","content":"<span class='clj-double'>20639.0</span>","value":"20639.0"}],"value":"[:A00100 20639.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07100</span>","value":":N07100"},{"type":"html","content":"<span class='clj-double'>190.0</span>","value":"190.0"}],"value":"[:N07100 190.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:STATE</span>","value":":STATE"},{"type":"html","content":"<span class='clj-string'>&quot;AL&quot;</span>","value":"\"AL\""}],"value":"[:STATE \"AL\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A11070</span>","value":":A11070"},{"type":"html","content":"<span class='clj-double'>435.0</span>","value":"435.0"}],"value":"[:A11070 435.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A59660</span>","value":":A59660"},{"type":"html","content":"<span class='clj-double'>1472.0</span>","value":"1472.0"}],"value":"[:A59660 1472.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00200</span>","value":":N00200"},{"type":"html","content":"<span class='clj-double'>1230.0</span>","value":"1230.0"}],"value":"[:N00200 1230.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00650</span>","value":":A00650"},{"type":"html","content":"<span class='clj-double'>154.0</span>","value":"154.0"}],"value":"[:A00650 154.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00600</span>","value":":N00600"},{"type":"html","content":"<span class='clj-double'>90.0</span>","value":"90.0"}],"value":"[:N00600 90.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00101</span>","value":":N00101"},{"type":"html","content":"<span class='clj-double'>160.0</span>","value":"160.0"}],"value":"[:N00101 160.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:AGI_STUB</span>","value":":AGI_STUB"},{"type":"html","content":"<span class='clj-double'>1.0</span>","value":"1.0"}],"value":"[:AGI_STUB 1.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N04800</span>","value":":N04800"},{"type":"html","content":"<span class='clj-double'>710.0</span>","value":"710.0"}],"value":"[:N04800 710.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A11901</span>","value":":A11901"},{"type":"html","content":"<span class='clj-double'>107.0</span>","value":"107.0"}],"value":"[:A11901 107.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00600</span>","value":":A00600"},{"type":"html","content":"<span class='clj-double'>228.0</span>","value":"228.0"}],"value":"[:A00600 228.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A02500</span>","value":":A02500"},{"type":"html","content":"<span class='clj-double'>162.0</span>","value":"162.0"}],"value":"[:A02500 162.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N11902</span>","value":":N11902"},{"type":"html","content":"<span class='clj-double'>1370.0</span>","value":"1370.0"}],"value":"[:N11902 1370.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N10300</span>","value":":N10300"},{"type":"html","content":"<span class='clj-double'>760.0</span>","value":"760.0"}],"value":"[:N10300 760.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N01400</span>","value":":N01400"},{"type":"html","content":"<span class='clj-double'>110.0</span>","value":"110.0"}],"value":"[:N01400 110.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:zipcode</span>","value":":zipcode"},{"type":"html","content":"<span class='clj-double'>35004.0</span>","value":"35004.0"}],"value":"[:zipcode 35004.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07100</span>","value":":A07100"},{"type":"html","content":"<span class='clj-double'>68.0</span>","value":"68.0"}],"value":"[:A07100 68.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:STATEFIPS</span>","value":":STATEFIPS"},{"type":"html","content":"<span class='clj-string'>&quot;01&quot;</span>","value":"\"01\""}],"value":"[:STATEFIPS \"01\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18450</span>","value":":A18450"},{"type":"html","content":"<span class='clj-double'>75.0</span>","value":"75.0"}],"value":"[:A18450 75.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A01700</span>","value":":A01700"},{"type":"html","content":"<span class='clj-double'>2224.0</span>","value":"2224.0"}],"value":"[:A01700 2224.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00900</span>","value":":A00900"},{"type":"html","content":"<span class='clj-double'>1528.0</span>","value":"1528.0"}],"value":"[:A00900 1528.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N1</span>","value":":N1"},{"type":"html","content":"<span class='clj-double'>1600.0</span>","value":"1600.0"}],"value":"[:N1 1600.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N02500</span>","value":":N02500"},{"type":"html","content":"<span class='clj-double'>90.0</span>","value":"90.0"}],"value":"[:N02500 90.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A59720</span>","value":":A59720"},{"type":"html","content":"<span class='clj-double'>1313.0</span>","value":"1313.0"}],"value":"[:A59720 1313.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N09600</span>","value":":N09600"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:N09600 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00101</span>","value":":A00101"},{"type":"html","content":"<span class='clj-double'>2455.0</span>","value":"2455.0"}],"value":"[:A00101 2455.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07260</span>","value":":N07260"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:N07260 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A09600</span>","value":":A09600"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:A09600 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A06500</span>","value":":A06500"},{"type":"html","content":"<span class='clj-double'>397.0</span>","value":"397.0"}],"value":"[:A06500 397.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07180</span>","value":":A07180"},{"type":"html","content":"<span class='clj-double'>7.0</span>","value":"7.0"}],"value":"[:A07180 7.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS2</span>","value":":MARS2"},{"type":"html","content":"<span class='clj-double'>270.0</span>","value":"270.0"}],"value":"[:MARS2 270.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N59660</span>","value":":N59660"},{"type":"html","content":"<span class='clj-double'>560.0</span>","value":"560.0"}],"value":"[:N59660 560.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A11902</span>","value":":A11902"},{"type":"html","content":"<span class='clj-double'>2855.0</span>","value":"2855.0"}],"value":"[:A11902 2855.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N01000</span>","value":":N01000"},{"type":"html","content":"<span class='clj-double'>60.0</span>","value":"60.0"}],"value":"[:N01000 60.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N07180</span>","value":":N07180"},{"type":"html","content":"<span class='clj-double'>20.0</span>","value":"20.0"}],"value":"[:N07180 20.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A04470</span>","value":":A04470"},{"type":"html","content":"<span class='clj-double'>2150.0</span>","value":"2150.0"}],"value":"[:A04470 2150.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N04470</span>","value":":N04470"},{"type":"html","content":"<span class='clj-double'>160.0</span>","value":"160.0"}],"value":"[:N04470 160.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00900</span>","value":":N00900"},{"type":"html","content":"<span class='clj-double'>230.0</span>","value":"230.0"}],"value":"[:N00900 230.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N19300</span>","value":":N19300"},{"type":"html","content":"<span class='clj-double'>110.0</span>","value":"110.0"}],"value":"[:N19300 110.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18425</span>","value":":N18425"},{"type":"html","content":"<span class='clj-double'>60.0</span>","value":"60.0"}],"value":"[:N18425 60.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS1</span>","value":":MARS1"},{"type":"html","content":"<span class='clj-double'>990.0</span>","value":"990.0"}],"value":"[:MARS1 990.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A03300</span>","value":":A03300"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:A03300 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A04800</span>","value":":A04800"},{"type":"html","content":"<span class='clj-double'>4379.0</span>","value":"4379.0"}],"value":"[:A04800 4379.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N02300</span>","value":":N02300"},{"type":"html","content":"<span class='clj-double'>90.0</span>","value":"90.0"}],"value":"[:N02300 90.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A10300</span>","value":":A10300"},{"type":"html","content":"<span class='clj-double'>667.0</span>","value":"667.0"}],"value":"[:A10300 667.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00300</span>","value":":A00300"},{"type":"html","content":"<span class='clj-double'>246.0</span>","value":"246.0"}],"value":"[:A00300 246.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:SCHF</span>","value":":SCHF"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:SCHF 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N03300</span>","value":":N03300"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:N03300 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18425</span>","value":":A18425"},{"type":"html","content":"<span class='clj-double'>51.0</span>","value":"51.0"}],"value":"[:A18425 51.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N18450</span>","value":":N18450"},{"type":"html","content":"<span class='clj-double'>80.0</span>","value":"80.0"}],"value":"[:N18450 80.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A18300</span>","value":":A18300"},{"type":"html","content":"<span class='clj-double'>206.0</span>","value":"206.0"}],"value":"[:A18300 206.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00200</span>","value":":A00200"},{"type":"html","content":"<span class='clj-double'>15308.0</span>","value":"15308.0"}],"value":"[:A00200 15308.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A07260</span>","value":":A07260"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:A07260 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A19700</span>","value":":A19700"},{"type":"html","content":"<span class='clj-double'>282.0</span>","value":"282.0"}],"value":"[:A19700 282.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00650</span>","value":":N00650"},{"type":"html","content":"<span class='clj-double'>80.0</span>","value":"80.0"}],"value":"[:N00650 80.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N11070</span>","value":":N11070"},{"type":"html","content":"<span class='clj-double'>370.0</span>","value":"370.0"}],"value":"[:N11070 370.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N06500</span>","value":":N06500"},{"type":"html","content":"<span class='clj-double'>570.0</span>","value":"570.0"}],"value":"[:N06500 570.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:PREP</span>","value":":PREP"},{"type":"html","content":"<span class='clj-double'>840.0</span>","value":"840.0"}],"value":"[:PREP 840.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A02300</span>","value":":A02300"},{"type":"html","content":"<span class='clj-double'>433.0</span>","value":"433.0"}],"value":"[:A02300 433.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N59720</span>","value":":N59720"},{"type":"html","content":"<span class='clj-double'>500.0</span>","value":"500.0"}],"value":"[:N59720 500.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N00300</span>","value":":N00300"},{"type":"html","content":"<span class='clj-double'>260.0</span>","value":"260.0"}],"value":"[:N00300 260.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:NUMDEP</span>","value":":NUMDEP"},{"type":"html","content":"<span class='clj-double'>760.0</span>","value":"760.0"}],"value":"[:NUMDEP 760.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:N11901</span>","value":":N11901"},{"type":"html","content":"<span class='clj-double'>130.0</span>","value":"130.0"}],"value":"[:N11901 130.0]"}],"value":"{:N2 2390.0, :A19300 635.0, :MARS4 300.0, :N07220 60.0, :N19700 130.0, :A18500 59.0, :A01400 730.0, :N01700 240.0, :N18500 110.0, :A01000 -59.0, :N18300 160.0, :A07220 19.0, :A00100 20639.0, :N07100 190.0, :STATE \"AL\", :A11070 435.0, :A59660 1472.0, :N00200 1230.0, :A00650 154.0, :N00600 90.0, :N00101 160.0, :AGI_STUB 1.0, :N04800 710.0, :A11901 107.0, :A00600 228.0, :A02500 162.0, :N11902 1370.0, :N10300 760.0, :N01400 110.0, :zipcode 35004.0, :A07100 68.0, :STATEFIPS \"01\", :A18450 75.0, :A01700 2224.0, :A00900 1528.0, :N1 1600.0, :N02500 90.0, :A59720 1313.0, :N09600 0.0, :A00101 2455.0, :N07260 0.0, :A09600 0.0, :A06500 397.0, :A07180 7.0, :MARS2 270.0, :N59660 560.0, :A11902 2855.0, :N01000 60.0, :N07180 20.0, :A04470 2150.0, :N04470 160.0, :N00900 230.0, :N19300 110.0, :N18425 60.0, :MARS1 990.0, :A03300 0.0, :A04800 4379.0, :N02300 90.0, :A10300 667.0, :A00300 246.0, :SCHF 0.0, :N03300 0.0, :A18425 51.0, :N18450 80.0, :A18300 206.0, :A00200 15308.0, :A07260 0.0, :A19700 282.0, :N00650 80.0, :N11070 370.0, :N06500 570.0, :PREP 840.0, :A02300 433.0, :N59720 500.0, :N00300 260.0, :NUMDEP 760.0, :N11901 130.0}"}],"value":"[{:N2 2390.0, :A19300 635.0, :MARS4 300.0, :N07220 60.0, :N19700 130.0, :A18500 59.0, :A01400 730.0, :N01700 240.0, :N18500 110.0, :A01000 -59.0, :N18300 160.0, :A07220 19.0, :A00100 20639.0, :N07100 190.0, :STATE \"AL\", :A11070 435.0, :A59660 1472.0, :N00200 1230.0, :A00650 154.0, :N00600 90.0, :N00101 160.0, :AGI_STUB 1.0, :N04800 710.0, :A11901 107.0, :A00600 228.0, :A02500 162.0, :N11902 1370.0, :N10300 760.0, :N01400 110.0, :zipcode 35004.0, :A07100 68.0, :STATEFIPS \"01\", :A18450 75.0, :A01700 2224.0, :A00900 1528.0, :N1 1600.0, :N02500 90.0, :A59720 1313.0, :N09600 0.0, :A00101 2455.0, :N07260 0.0, :A09600 0.0, :A06500 397.0, :A07180 7.0, :MARS2 270.0, :N59660 560.0, :A11902 2855.0, :N01000 60.0, :N07180 20.0, :A04470 2150.0, :N04470 160.0, :N00900 230.0, :N19300 110.0, :N18425 60.0, :MARS1 990.0, :A03300 0.0, :A04800 4379.0, :N02300 90.0, :A10300 667.0, :A00300 246.0, :SCHF 0.0, :N03300 0.0, :A18425 51.0, :N18450 80.0, :A18300 206.0, :A00200 15308.0, :A07260 0.0, :A19700 282.0, :N00650 80.0, :N11070 370.0, :N06500 570.0, :PREP 840.0, :A02300 433.0, :N59720 500.0, :N00300 260.0, :NUMDEP 760.0, :N11901 130.0}]"}
;; <=

;; **
;;; ## Curried Reductions
;;; 
;;; Let's create **curried** versions of our steps
;; **

;; @@
(def line-formatter
  (r/map parse-line))

(defn record-formatter
  [column-names]
  (r/map (fn [fields]
           (zipmap column-names fields))))

(def remove-zero-zip
  (r/remove (fn [record]
              (zero? (:zipcode record)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/remove-zero-zip</span>","value":"#'cljds.ch5.ws/remove-zero-zip"}
;; <=

;; @@
(defn load-data
  "Load .csv data into a parallelizable structure"
  [file]
  (let [data (iota/seq file)
        col-names (parse-columns (first data))
        parse-file (comp remove-zero-zip
                         (record-formatter col-names)
                         line-formatter)]
    (parse-file (rest data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/load-data</span>","value":"#'cljds.ch5.ws/load-data"}
;; <=

;; **
;;; ## Statistical Folds
;;; 
;;; We can create statistical folds to perform some analysis
;; **

;; @@
(time
  (let [data (load-data "data/soi.csv")
        xs (into [] (r/map :N1 data))]
    (/ (reduce + xs)
       (count xs))))
;; @@
;; ->
;;; &quot;Elapsed time: 21834.629 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-double'>853.3700884764523</span>","value":"853.3700884764523"}
;; <=

;; **
;;; The issue with the previous code is that we pass over the whole dataset three times: one to create xs, one to calculate the sum, and one to calculate the count.
;;; 
;;; Since the **mean** is not an associative function, we need to create 2 different functions: `mean-combiner` and `mean-reducer`
;; **

;; @@
(defn mean-combiner
  ([] {:count 0 :sum 0})
  ([a b] (merge-with + a b)))

(defn mean-reducer
  [acc x]
  (-> acc
      (update-in [:count] inc)
      (update-in [:sum] + x)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/mean-reducer</span>","value":"#'cljds.ch5.ws/mean-reducer"}
;; <=

;; @@
(time
  (->> (load-data "data/soi.csv")
       (r/map :N1)
       (r/fold mean-combiner
               mean-reducer)))
;; @@
;; ->
;;; &quot;Elapsed time: 2204.521 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:count</span>","value":":count"},{"type":"html","content":"<span class='clj-long'>166598</span>","value":"166598"}],"value":"[:count 166598]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sum</span>","value":":sum"},{"type":"html","content":"<span class='clj-double'>1.4216975E8</span>","value":"1.4216975E8"}],"value":"[:sum 1.4216975E8]"}],"value":"{:count 166598, :sum 1.4216975E8}"}
;; <=

;; @@
(defn mean-post-combiner
  [{:keys [count sum]}]
  (if (zero? count) 0 (/ sum count)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/mean-post-combiner</span>","value":"#'cljds.ch5.ws/mean-post-combiner"}
;; <=

;; @@
(time
  (->> (load-data "data/soi.csv")
       (r/map :N1)
       (r/fold mean-combiner
               mean-reducer)
       (mean-post-combiner)))
;; @@
;; ->
;;; &quot;Elapsed time: 2028.566 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-double'>853.3700884764523</span>","value":"853.3700884764523"}
;; <=

;; **
;;; ## Variance Fold
;;; 
;;; ### The dumb way
;; **

;; @@
(let [data (->> (load-data "data/soi.csv")
                (r/map :N1))
      mean-x (->> data
                  (r/fold mean-combiner
                          mean-reducer)
                  mean-post-combiner)
      sq-diff (fn [x] (i/pow (- x mean-x) 2))]
  (->> data
       (r/map sq-diff)
       (r/fold mean-combiner
               mean-reducer)
       (mean-post-combiner)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>3144836.857368185</span>","value":"3144836.857368185"}
;; <=

;; **
;;; ### The smart way
;; **

;; @@
(defn variance-combiner
  ([] {:count 0 :mean 0 :sum-of-squares 0})
  ([a b]
   
   (let [count (+ (:count a) (:count b))]
     
     {:count count
      
      :mean (/ (+ (* (:count a) (:mean a))
                  (* (:count b) (:mean b)))
               count)
      
      :sum-of-squares (+ (:sum-of-squares a)
                         (:sum-of-squares b)
                         (/ (* (- (:mean b)
                                  (:mean a))
                               (- (:mean b)
                                  (:mean a))
                               (:count a)
                               (:count b))
                            count))})))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/variance-combiner</span>","value":"#'cljds.ch5.ws/variance-combiner"}
;; <=

;; @@
(defn variance-reducer
  [{:keys [count mean sum-of-squares]} x]
  (let [count' (inc count)
        mean' (+ mean (/ (- x mean) count'))]
    {:count count'
     :mean mean'
     :sum-of-squares (+ sum-of-squares
                        (* (- x mean') (- x mean)))}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/variance-reducer</span>","value":"#'cljds.ch5.ws/variance-reducer"}
;; <=

;; @@
(defn variance-post-combiner
  [{:keys [count mean sum-of-squares]}]
  (if (zero? count) 0 (/ sum-of-squares count)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/variance-post-combiner</span>","value":"#'cljds.ch5.ws/variance-post-combiner"}
;; <=

;; @@
(->> (load-data "data/soi.csv")
     (r/map :N1)
     (r/fold variance-combiner
             variance-reducer)
     (variance-post-combiner))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>3144836.857368185</span>","value":"3144836.857368185"}
;; <=

;; **
;;; ## Covariance
;;; 
;;; Luckily **Tesser** already gives us some predefined mathematical folds. 
;; **

;; @@
(let [data (into [] (load-data "data/soi.csv"))]
  (->> (m/covariance :A02300 :A00200)
       (t/tesser (t/chunk 512 data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>3.495780077886466E7</span>","value":"3.495780077886466E7"}
;; <=

;; @@
(defn chunks
  [coll]
  (->> (into [] coll)
       (t/chunk 1024)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/chunks</span>","value":"#'cljds.ch5.ws/chunks"}
;; <=

;; **
;;; We should rewrite the `load-data` function since **Tesser** doesn't really care about the order of things, but we take the first row of the file as columns names
;; **

;; @@
(def column-names
  (->> (iota/seq "data/soi.csv")
       (first)
       (parse-columns)
       (into [])))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/column-names</span>","value":"#'cljds.ch5.ws/column-names"}
;; <=

;; @@
(defn prepare-data
  []
  (->> (t/remove #(.startsWith % "STATEFIPS"))
       (t/map parse-line)
       (t/map (partial format-record column-names))
       (t/remove #(zero? (:zipcode %)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/prepare-data</span>","value":"#'cljds.ch5.ws/prepare-data"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (m/covariance :A02300 :A00200)
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>3.495780077886466E7</span>","value":"3.495780077886466E7"}
;; <=

;; **
;;; ## Correlation
;; **

;; @@
(let [data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (m/correlation :A02300 :A00200)
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.35378378685923106</span>","value":"0.35378378685923106"}
;; <=

;; **
;;; ## Simple Linear Regression
;;; 
;;; As we previously saw, **Linear Regression** can be calculated as:
;;; 
;;; $$
;;; r = \frac{cov(X,Y)}{var(X)}
;;; $$
;;; $$
;;; a=\bar{y}-b\bar{x}
;;; $$
;; **

;; @@
(doc t/fuse)
(doc t/facet)
;; @@
;; ->
;;; -------------------------
;;; tesser.core/fuse
;;; ([fold-map] [fold-map fold__12147__auto__])
;;;   You&#x27;ve got several folds, and want to execute them in one pass. Fuse is the
;;;   function for you! It takes a map from keys to folds, like
;;; 
;;;       (-&gt;&gt; (map parse-person)
;;;            (fuse {:age-range    (-&gt;&gt; (map :age) (range))
;;;                   :colors-prefs (-&gt;&gt; (map :favorite-color) (frequencies))})
;;;            (tesser people))
;;; 
;;;   And returns a map from those same keys to the results of the corresponding
;;;   folds:
;;; 
;;;       {:age-range   [0 74],
;;;        :color-prefs {:red        120
;;;                      :blue       312
;;;                      :watermelon 1953
;;;                      :imhotep    1}}
;;; 
;;;   Note that this fold only invokes `parse-person` once for each record, and
;;;   completes in a single pass. If we ran the age and color folds independently,
;;;   it&#x27;d take two passes over the dataset--and require parsing every person
;;;   *twice*.
;;; 
;;;   Fuse and facet both return maps, but generalize over different axes. Fuse
;;;   applies a fixed set of *independent* folds over the *same* inputs, where
;;;   facet applies the *same* fold to a dynamic set of keys taken from the
;;;   inputs.
;;; 
;;;   Note that fuse compiles the folds you pass to it, so you need to build them
;;;   completely *before* fusing. The fold `fuse` returns can happily be combined
;;;   with other transformations at its level, but its internal folds are sealed
;;;   and opaque.
;;; -------------------------
;;; tesser.core/facet
;;; ([] [fold__12147__auto__])
;;;   Your inputs are maps, and you want to apply a fold to each value
;;;   independently. Facet generalizes a fold over a single value to operate on
;;;   maps of keys to those values, returning a map of keys to the results of the
;;;   fold over all values for that key. Each key gets an independent instance of
;;;   the fold.
;;; 
;;;   For instance, say you have inputs like
;;; 
;;;       {:x 1, :y 2}
;;;       {}
;;;       {:y 3, :z 4}
;;; 
;;;   Then the fold
;;; 
;;;       (-&gt;&gt; (facet)
;;;            (mean))
;;; 
;;;   returns a map for each key&#x27;s mean value:
;;; 
;;;       {:x 1, :y 2, :z 4}
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (t/map :A00200)
       (t/fuse {:A00200-mean (m/mean)
                :A00200-sd (m/standard-deviation)})
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00200-mean</span>","value":":A00200-mean"},{"type":"html","content":"<span class='clj-double'>37290.58880658831</span>","value":"37290.58880658831"}],"value":"[:A00200-mean 37290.58880658831]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00200-sd</span>","value":":A00200-sd"},{"type":"html","content":"<span class='clj-double'>89965.99846545045</span>","value":"89965.99846545045"}],"value":"[:A00200-sd 89965.99846545045]"}],"value":"{:A00200-mean 37290.58880658831, :A00200-sd 89965.99846545045}"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (t/map #(select-keys % [:A00200 :A02300]))
       (t/facet)
       (m/mean)
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00200</span>","value":":A00200"},{"type":"html","content":"<span class='clj-double'>37290.58880658831</span>","value":"37290.58880658831"}],"value":"[:A00200 37290.58880658831]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A02300</span>","value":":A02300"},{"type":"html","content":"<span class='clj-double'>419.67862159209596</span>","value":"419.67862159209596"}],"value":"[:A02300 419.67862159209596]"}],"value":"{:A00200 37290.58880658831, :A02300 419.67862159209596}"}
;; <=

;; @@
(defn calculate-coefficients
  [{:keys [covariance variance-x mean-x mean-y]}]
  (let [slope (/ covariance variance-x)
        intercept (- mean-y (* mean-x slope))]
    [intercept slope]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/calculate-coefficients</span>","value":"#'cljds.ch5.ws/calculate-coefficients"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")
      fx :A00200
      fy :A02300]
  (->> (prepare-data)
       (t/fuse {:covariance (m/covariance fx fy)
                :variance-x (m/variance (t/map fx))
                :mean-x (m/mean (t/map fx))
                :mean-y (m/mean (t/map fy))})
       (t/post-combine calculate-coefficients)
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>258.6190515572913</span>","value":"258.6190515572913"},{"type":"html","content":"<span class='clj-double'>0.004319040679946289</span>","value":"0.004319040679946289"}],"value":"[258.6190515572913 0.004319040679946289]"}
;; <=

;; **
;;; ## Correlation Matrix
;; **

;; @@
(let [data (iota/seq "data/soi.csv")
      attributes {:unemployment :A02300
                  :salary		:A00200
                  :income		:AGI_STUB
                  :submissions	:MARS2
                  :dependents	:NUMDEP}]
  (->> (prepare-data)
       (m/correlation-matrix attributes)
       (t/tesser (chunks data))
       (clojure.pprint/pprint)))
;; @@
;; ->
;;; {[:salary :dependents] 0.5377718342369946,
;;;  [:submissions :unemployment] 0.6025773943975872,
;;;  [:submissions :income] -0.028202512657224954,
;;;  [:submissions :dependents] 0.7718554814702941,
;;;  [:income :unemployment] -0.27477864920971695,
;;;  [:unemployment :income] -0.27477864920971695,
;;;  [:salary :submissions] 0.7780779140465943,
;;;  [:dependents :income] -0.20312748087736157,
;;;  [:unemployment :salary] 0.3537837868592312,
;;;  [:dependents :unemployment] 0.8211733976776759,
;;;  [:dependents :salary] 0.5377718342369946,
;;;  [:submissions :salary] 0.7780779140465943,
;;;  [:income :dependents] -0.20312748087736157,
;;;  [:salary :income] 0.11888262708520696,
;;;  [:unemployment :submissions] 0.6025773943975872,
;;;  [:dependents :submissions] 0.7718554814702941,
;;;  [:income :submissions] -0.028202512657224954,
;;;  [:salary :unemployment] 0.3537837868592312,
;;;  [:unemployment :dependents] 0.8211733976776759,
;;;  [:income :salary] 0.11888262708520696}
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Multiple Regression
;; **

;; @@
(defn feature-scales
  [features]
  (->> (prepare-data)
       (t/map #(select-keys % features))
       (t/facet)
       (t/fuse {:mean (m/mean)
                :sd (m/standard-deviation)})))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/feature-scales</span>","value":"#'cljds.ch5.ws/feature-scales"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")
      features [:A02300 :A00200 :AGI_STUB
                :NUMDEP :MARS2]]
  (->> (feature-scales features)
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A02300</span>","value":":A02300"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:mean</span>","value":":mean"},{"type":"html","content":"<span class='clj-double'>419.67862159209596</span>","value":"419.67862159209596"}],"value":"[:mean 419.67862159209596]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sd</span>","value":":sd"},{"type":"html","content":"<span class='clj-double'>1098.3237615539792</span>","value":"1098.3237615539792"}],"value":"[:sd 1098.3237615539792]"}],"value":"{:mean 419.67862159209596, :sd 1098.3237615539792}"}],"value":"[:A02300 {:mean 419.67862159209596, :sd 1098.3237615539792}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00200</span>","value":":A00200"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:mean</span>","value":":mean"},{"type":"html","content":"<span class='clj-double'>37290.58880658831</span>","value":"37290.58880658831"}],"value":"[:mean 37290.58880658831]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sd</span>","value":":sd"},{"type":"html","content":"<span class='clj-double'>89965.99846545044</span>","value":"89965.99846545044"}],"value":"[:sd 89965.99846545044]"}],"value":"{:mean 37290.58880658831, :sd 89965.99846545044}"}],"value":"[:A00200 {:mean 37290.58880658831, :sd 89965.99846545044}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:AGI_STUB</span>","value":":AGI_STUB"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:mean</span>","value":":mean"},{"type":"html","content":"<span class='clj-double'>3.499939975269811</span>","value":"3.499939975269811"}],"value":"[:mean 3.499939975269811]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sd</span>","value":":sd"},{"type":"html","content":"<span class='clj-double'>1.7079052308118514</span>","value":"1.7079052308118514"}],"value":"[:sd 1.7079052308118514]"}],"value":"{:mean 3.499939975269811, :sd 1.7079052308118514}"}],"value":"[:AGI_STUB {:mean 3.499939975269811, :sd 1.7079052308118514}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:NUMDEP</span>","value":":NUMDEP"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:mean</span>","value":":mean"},{"type":"html","content":"<span class='clj-double'>581.8504423822615</span>","value":"581.8504423822615"}],"value":"[:mean 581.8504423822615]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sd</span>","value":":sd"},{"type":"html","content":"<span class='clj-double'>1309.1973488789336</span>","value":"1309.1973488789336"}],"value":"[:sd 1309.1973488789336]"}],"value":"{:mean 581.8504423822615, :sd 1309.1973488789336}"}],"value":"[:NUMDEP {:mean 581.8504423822615, :sd 1309.1973488789336}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS2</span>","value":":MARS2"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:mean</span>","value":":mean"},{"type":"html","content":"<span class='clj-double'>317.0412009748016</span>","value":"317.0412009748016"}],"value":"[:mean 317.0412009748016]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sd</span>","value":":sd"},{"type":"html","content":"<span class='clj-double'>533.4496892658645</span>","value":"533.4496892658645"}],"value":"[:sd 533.4496892658645]"}],"value":"{:mean 317.0412009748016, :sd 533.4496892658645}"}],"value":"[:MARS2 {:mean 317.0412009748016, :sd 533.4496892658645}]"}],"value":"{:A02300 {:mean 419.67862159209596, :sd 1098.3237615539792}, :A00200 {:mean 37290.58880658831, :sd 89965.99846545044}, :AGI_STUB {:mean 3.499939975269811, :sd 1.7079052308118514}, :NUMDEP {:mean 581.8504423822615, :sd 1309.1973488789336}, :MARS2 {:mean 317.0412009748016, :sd 533.4496892658645}}"}
;; <=

;; @@
(defn scale-features
  [factors]
  (let [f (fn [x {:keys [mean sd]}]
            (/ (- x mean) sd))]
    (fn [x]
      (merge-with f x factors))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/scale-features</span>","value":"#'cljds.ch5.ws/scale-features"}
;; <=

;; @@
(defn unscale-features
  [factors]
  (let [f (fn [x {:keys [mean sd]}]
            (+ (* x sd) mean))]
    (fn [x]
      (merge-with f x factors))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/unscale-features</span>","value":"#'cljds.ch5.ws/unscale-features"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")
      features [:A02300 :A00200 :AGI_STUB
                :NUMDEP :MARS2]
      factors (->> (feature-scales features)
                   (t/tesser (chunks data)))]
  (->> (load-data "data/soi.csv")
       (r/map #(select-keys % features))
       (r/map (scale-features factors))
       (into [])
       first))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A02300</span>","value":":A02300"},{"type":"html","content":"<span class='clj-double'>0.012128826557531716</span>","value":"0.012128826557531716"}],"value":"[:A02300 0.012128826557531716]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:A00200</span>","value":":A00200"},{"type":"html","content":"<span class='clj-double'>-0.2443432983743327</span>","value":"-0.2443432983743327"}],"value":"[:A00200 -0.2443432983743327]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:AGI_STUB</span>","value":":AGI_STUB"},{"type":"html","content":"<span class='clj-double'>-1.4637463075638375</span>","value":"-1.4637463075638375"}],"value":"[:AGI_STUB -1.4637463075638375]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:NUMDEP</span>","value":":NUMDEP"},{"type":"html","content":"<span class='clj-double'>0.13607540358242254</span>","value":"0.13607540358242254"}],"value":"[:NUMDEP 0.13607540358242254]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:MARS2</span>","value":":MARS2"},{"type":"html","content":"<span class='clj-double'>-0.08818301317138241</span>","value":"-0.08818301317138241"}],"value":"[:MARS2 -0.08818301317138241]"}],"value":"{:A02300 0.012128826557531716, :A00200 -0.2443432983743327, :AGI_STUB -1.4637463075638375, :NUMDEP 0.13607540358242254, :MARS2 -0.08818301317138241}"}
;; <=

;; @@
(defn feature-matrix
  [record features]
  (let [xs (map #(% record) features)]
    (i/matrix (cons 1 xs))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/feature-matrix</span>","value":"#'cljds.ch5.ws/feature-matrix"}
;; <=

;; @@
(defn extract-features
  [fy features]
  (fn [record]
    {:y (fy record)
     :xs (feature-matrix record features)}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/extract-features</span>","value":"#'cljds.ch5.ws/extract-features"}
;; <=

;; @@
(let [data (iota/seq "data/soi.csv")
      features [:A02300 :A00200 :AGI_STUB
                :NUMDEP :MARS2]
      factors (->> (feature-scales features)
                   (t/tesser (chunks data)))]
  (->> (load-data "data/soi.csv")
       (r/map (scale-features factors))
       (r/map (extract-features :A02300 features))
       (into [])
       (first)))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:y</span>","value":":y"},{"type":"html","content":"<span class='clj-double'>0.012128826557531714</span>","value":"0.012128826557531714"}],"value":"[:y 0.012128826557531714]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:xs</span>","value":":xs"},{"type":"html","content":"<span class='clj-unkown'> A 6x1 matrix\n -------------\n 1.00e+00 \n 1.21e-02 \n-2.44e-01 \n ... \n-1.46e+00 \n 1.36e-01 \n-8.82e-02 \n</span>","value":" A 6x1 matrix\n -------------\n 1.00e+00 \n 1.21e-02 \n-2.44e-01 \n ... \n-1.46e+00 \n 1.36e-01 \n-8.82e-02 \n"}],"value":"[:xs  A 6x1 matrix\n -------------\n 1.00e+00 \n 1.21e-02 \n-2.44e-01 \n ... \n-1.46e+00 \n 1.36e-01 \n-8.82e-02 \n]"}],"value":"{:y 0.012128826557531714, :xs  A 6x1 matrix\n -------------\n 1.00e+00 \n 1.21e-02 \n-2.44e-01 \n ... \n-1.46e+00 \n 1.36e-01 \n-8.82e-02 \n}"}
;; <=

;; @@
(defn matrix-sum
  [nrows ncols]
  (let [zeros-matrix (i/matrix 0 nrows ncols)]
    {:reducer-identity (constantly zeros-matrix)
     :reducer i/plus
     :combiner-identity (constantly zeros-matrix)
     :combiner i/plus}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/matrix-sum</span>","value":"#'cljds.ch5.ws/matrix-sum"}
;; <=

;; @@
(let [cols [:A02300 :A00200 :AGI_STUB
            :NUMDEP :MARS2]
      data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (t/map (extract-features :A02300 cols))
       (t/map :xs)
       (t/fold (matrix-sum 
                 (inc (count cols)) 1))
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 6x1 matrix\n -------------\n 1.67e+05 \n 6.99e+07 \n 6.21e+09 \n ... \n 5.83e+05 \n 9.69e+07 \n 5.28e+07 \n</span>","value":" A 6x1 matrix\n -------------\n 1.67e+05 \n 6.99e+07 \n 6.21e+09 \n ... \n 5.83e+05 \n 9.69e+07 \n 5.28e+07 \n"}
;; <=

;; @@
(defn calculate-error
  [coefs-t]
  (fn [{:keys [y xs]}]
    (let [y-hat (first (i/mmult coefs-t xs))
          error (- y-hat y)]
      (i/mult xs error))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/calculate-error</span>","value":"#'cljds.ch5.ws/calculate-error"}
;; <=

;; @@
(let [cols [:A02300 :A00200 :AGI_STUB
            :NUMDEP :MARS2]
      fcount (inc (count cols))
      coefs (vec (repeat fcount 0))
      data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (t/map (extract-features :A02300 cols))
       (t/map (calculate-error (i/trans coefs)))
       (t/fold (matrix-sum fcount 1))
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 6x1 matrix\n -------------\n-6.99e+07 \n-2.30e+11 \n-8.43e+12 \n ... \n-1.59e+08 \n-2.37e+11 \n-8.10e+10 \n</span>","value":" A 6x1 matrix\n -------------\n-6.99e+07 \n-2.30e+11 \n-8.43e+12 \n ... \n-1.59e+08 \n-2.37e+11 \n-8.10e+10 \n"}
;; <=

;; @@
(let [cols [:A02300 :A00200 :AGI_STUB
            :NUMDEP :MARS2]
      fcount (inc (count cols))
      coefs (vec (repeat fcount 0))
      data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (t/map (extract-features :A02300 cols))
       (t/map (calculate-error (i/trans coefs)))
       (t/fuse {:sum (t/fold (matrix-sum fcount 1))
                :count (t/count)})
       (t/post-combine (fn [{:keys [sum count]}]
                         (i/div sum count)))
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 6x1 matrix\n -------------\n-4.20e+02 \n-1.38e+06 \n-5.06e+07 \n ... \n-9.53e+02 \n-1.42e+06 \n-4.86e+05 \n</span>","value":" A 6x1 matrix\n -------------\n-4.20e+02 \n-1.38e+06 \n-5.06e+07 \n ... \n-9.53e+02 \n-1.42e+06 \n-4.86e+05 \n"}
;; <=

;; @@
(defn matrix-mean
  [nrows ncols]
  (let [zeros-matrix (i/matrix 0 nrows ncols)]
    {:reducer-identity (constantly [zeros-matrix 0])
     :reducer (fn [[sum counter] x]
                [(i/plus sum x) (inc counter)])
     :combiner-identity (constantly [zeros-matrix 0])
     :combiner (fn [[sum-a count-a] 
                    [sum-b count-b]]
                 [(i/plus sum-a sum-b)
                  (+ count-a count-b)])
     :post-combiner (fn [[sum count]]
                      (i/div sum count))}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/matrix-mean</span>","value":"#'cljds.ch5.ws/matrix-mean"}
;; <=

;; @@
(let [features [:A02300 :A00200 :AGI_STUB
                :NUMDEP :MARS2]
      fcount (inc (count features))
      coefs (vec (replicate fcount 0))
      data (iota/seq "data/soi.csv")]
  (->> (prepare-data)
       (t/map (extract-features :A02300 features))
       (t/map (calculate-error (i/trans coefs)))
       (t/fold (matrix-mean fcount 1))
       (t/tesser (chunks data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 6x1 matrix\n -------------\n-4.20e+02 \n-1.38e+06 \n-5.06e+07 \n ... \n-9.53e+02 \n-1.42e+06 \n-4.86e+05 \n</span>","value":" A 6x1 matrix\n -------------\n-4.20e+02 \n-1.38e+06 \n-5.06e+07 \n ... \n-9.53e+02 \n-1.42e+06 \n-4.86e+05 \n"}
;; <=

;; **
;;; ### A single step of gradient descent
;; **

;; @@
(defn update-coefficients
  [coefs alpha]
  (fn [cost]
    (->> (i/mult cost alpha)
         (i/minus coefs))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/update-coefficients</span>","value":"#'cljds.ch5.ws/update-coefficients"}
;; <=

;; @@
(defn gradient-descent-fold
  [{:keys [fy features factors
           coefs alpha]}]
  (let [zeros-matrix 
        (i/matrix 0 (count features) 1)]
    (->> (prepare-data)
         (t/map (scale-features factors))
         (t/map (extract-features fy features))
         (t/map (calculate-error (i/trans coefs)))
         (t/fold (matrix-mean (inc (count features)) 1))
         (t/post-combine (update-coefficients coefs alpha)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/gradient-descent-fold</span>","value":"#'cljds.ch5.ws/gradient-descent-fold"}
;; <=

;; @@
(let [features [:A02300 :A00200 :AGI_STUB
                :NUMDEP :MARS2]
      fcount (inc (count features))
      coefs (vec (replicate fcount 0))
      data (chunks (iota/seq "data/soi.csv"))
      factors (->> (feature-scales features)
                   (t/tesser data))
      options {:fy :A02300 :features features
               :factors factors :coefs coefs
               :alpha 0.1}]
  (->> (gradient-descent-fold options)
       (t/tesser data)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 6x1 matrix\n -------------\n 9.25e-17 \n 1.00e-01 \n 3.54e-02 \n ... \n-2.75e-02 \n 8.21e-02 \n 6.03e-02 \n</span>","value":" A 6x1 matrix\n -------------\n 9.25e-17 \n 1.00e-01 \n 3.54e-02 \n ... \n-2.75e-02 \n 8.21e-02 \n 6.03e-02 \n"}
;; <=

;; **
;;; ### Running iterative gradient descent
;; **

;; @@
(defn descend
  [options data]
  (fn [coefs]
    (->> (gradient-descent-fold 
           (assoc options :coefs coefs))
         (t/tesser data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch5.ws/descend</span>","value":"#'cljds.ch5.ws/descend"}
;; <=

;; @@
(let [features [:A02300 :A00200 :AGI_STUB
                :NUMDEP :MARS2]
      fcount (inc (count features))
      coefs (vec (replicate fcount 0))
      data (chunks (iota/seq "data/soi.csv"))
      factors (->> (feature-scales features)
                   (t/tesser data))
      options {:fy :A02300 :features features
               :factors factors :coefs coefs
               :alpha 0.1}
      iterations 10
      xs (range iterations)
      ys (->> (iterate (descend options data) coefs)
              (take iterations))]
  (-> (c/xy-plot xs (map first ys)
                 :x-label "Iterations"
                 :y-label "Coef")
      (c/add-lines xs (map second ys))
      (c/add-lines xs (map #(nth % 2) ys))
      (c/add-lines xs (map #(nth % 3) ys))
      (c/add-lines xs (map #(nth % 4) ys))
      (i/view)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>#object[org.jfree.chart.ChartFrame 0x39fee707 &quot;org.jfree.chart.ChartFrame[frame0,1280,0,500x400,layout=java.awt.BorderLayout,title=Incanter Plot,resizable,normal,defaultCloseOperation=DISPOSE_ON_CLOSE,rootPane=javax.swing.JRootPane[,0,0,500x400,layout=javax.swing.JRootPane$RootLayout,alignmentX=0.0,alignmentY=0.0,border=,flags=16777673,maximumSize=,minimumSize=,preferredSize=],rootPaneCheckingEnabled=true]&quot;]</span>","value":"#object[org.jfree.chart.ChartFrame 0x39fee707 \"org.jfree.chart.ChartFrame[frame0,1280,0,500x400,layout=java.awt.BorderLayout,title=Incanter Plot,resizable,normal,defaultCloseOperation=DISPOSE_ON_CLOSE,rootPane=javax.swing.JRootPane[,0,0,500x400,layout=javax.swing.JRootPane$RootLayout,alignmentX=0.0,alignmentY=0.0,border=,flags=16777673,maximumSize=,minimumSize=,preferredSize=],rootPaneCheckingEnabled=true]\"]"}
;; <=

;; @@

;; @@
