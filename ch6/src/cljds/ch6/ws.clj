;; gorilla-repl.fileformat = 1

;; **
;;; # Chapter 6 - Clustering
;; **

;; @@
(ns cljds.ch6.ws
  (:require [gorilla-plot.core :as plot]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [incanter.core :as i])
  (:import [org.apache.lucene.benchmark.utils ExtractReuters]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Data extraction
;;; 
;;; The **Reuters** dataset is in **SGML** format, we will make use of a ready made parser to extract it
;; **

;; @@
(defn sgml->txt
  [in-path out-path]
  (let [in-file  (clojure.java.io/file in-path)
        out-file (clojure.java.io/file out-path)]
    (.extract (ExtractReuters. in-file out-file))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/sgml-&gt;txt</span>","value":"#'cljds.ch6.ws/sgml->txt"}
;; <=

;; @@
;(sgml->txt "data/reuters-sgml" "data/reuters-text")
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Jaccard Index
;;; 
;;; The **Jaccard Index** (or similarity) is the set intersection divided by the set union:
;;; 
;;; $$
;;; J(A,B)=\frac{|A\cap B|}{|A\cup B|}
;;; $$
;; **

;; @@
(defn jaccard-similarity
  [a b]
  (let [a (set a)
        b (set b)]
    (/ (count (set/intersection a b))
       (count (set/union a b)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/jaccard-similarity</span>","value":"#'cljds.ch6.ws/jaccard-similarity"}
;; <=

;; @@
(let [a [1 2 3]
      b [2 3 4]]
  (jaccard-similarity a b))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-ratio'>1/2</span>","value":"1/2"}
;; <=

;; **
;;; ## Tokenization
;;; 
;;; To apply Jaccard similarity we have to tokenize the articles first
;; **

;; @@
(defn tokenize
  [s]
  (str/split s #"\W+"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/tokenize</span>","value":"#'cljds.ch6.ws/tokenize"}
;; <=

;; @@
(defn tokenize-reuters
  [content]
  (-> (str/replace content #"^.*\n\n" "")
      (str/lower-case)
      (tokenize)))

(defn reuters-terms
  [file]
  (-> (io/resource file)
      (slurp)
      (tokenize-reuters)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/reuters-terms</span>","value":"#'cljds.ch6.ws/reuters-terms"}
;; <=

;; @@
(let [a (set (reuters-terms "reut2-020.sgm-761.txt"))
      b (set (reuters-terms "reut2-007.sgm-750.txt"))
      s (jaccard-similarity a b)]
  (println "A: " a)
  (println "B: " b)
  (println "Similarity: " s))
;; @@
;; ->
;;; A:  #{recession says reagan sees no he}
;;; B:  #{bill transit says highway reagan and will veto he}
;;; Similarity:  1/4
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Bag-of-words
;; **

;; @@
(defn euclidean-distance
  [a b]
  (->> (map (comp i/sq -) a b)
       (apply +)
       (i/sqrt)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/euclidean-distance</span>","value":"#'cljds.ch6.ws/euclidean-distance"}
;; <=

;; @@

;; @@
