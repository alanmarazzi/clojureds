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
            [incanter.core :as i]
            [stemmers.core :as stemmer]
            [incanter.stats :as s]
            [me.raynes.fs :as mfs]
            [parkour (conf :as conf) (fs :as fs)
             ,       (toolbox :as ptb) (tool :as tool)]
            [parkour.graph :as pg]
            [parkour.io (dseq :as dseq) (text :as text) (avro :as mra)
             ,          (seqf :as seqf)
             ,          (dsink :as dsink)]
            [parkour.io.dux :as dux]
            [parkour.io.dval :as dval]
            [parkour.mapreduce :as mr]
			[parkour.wrapper :refer [Wrapper]]
            [clojure.core.reducers :as r]
            [transduce.reducers :as tr])
  (:import [org.apache.lucene.benchmark.utils ExtractReuters]
           [org.apache.mahout.text
            SequenceFilesFromDirectory]))
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

;; **
;;; After defining the **Euclidean distance** we have to find a way to keep track of words and count them in a fast and efficient way.
;;; 
;;; To achieve this goal we will build a **dictionary** keeping track of words appearing in all documents.
;; **

;; @@
(def dictionary
  (atom {:count 0
         :words {}}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/dictionary</span>","value":"#'cljds.ch6.ws/dictionary"}
;; <=

;; @@
(defn add-term-to-dict
  [dict word]
  (if (contains? (:terms dict) word)
    dict
    (-> dict
        (update-in [:terms] assoc word (get dict :count))
        (update-in [:count] inc))))

(defn add-term-to-dict!
  [dict term]
  (doto dict
    (swap! add-term-to-dict term)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/add-term-to-dict!</span>","value":"#'cljds.ch6.ws/add-term-to-dict!"}
;; <=

;; @@
(add-term-to-dict! dictionary "love")
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-atom'>#object[clojure.lang.Atom 0x77413334 {:status :ready, :val {:count 1, :words {}, :terms {&quot;love&quot; 0}}}]</span>","value":"#object[clojure.lang.Atom 0x77413334 {:status :ready, :val {:count 1, :words {}, :terms {\"love\" 0}}}]"}
;; <=

;; @@
(add-term-to-dict! dictionary "music")
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-atom'>#object[clojure.lang.Atom 0x77413334 {:status :ready, :val {:count 2, :words {}, :terms {&quot;love&quot; 0, &quot;music&quot; 1}}}]</span>","value":"#object[clojure.lang.Atom 0x77413334 {:status :ready, :val {:count 2, :words {}, :terms {\"love\" 0, \"music\" 1}}}]"}
;; <=

;; @@
(add-term-to-dict! dictionary "love")
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-atom'>#object[clojure.lang.Atom 0x77413334 {:status :ready, :val {:count 2, :words {}, :terms {&quot;love&quot; 0, &quot;music&quot; 1}}}]</span>","value":"#object[clojure.lang.Atom 0x77413334 {:status :ready, :val {:count 2, :words {}, :terms {\"love\" 0, \"music\" 1}}}]"}
;; <=

;; **
;;; By using an `atom` we make sure that each word gets its own index even when the dictionary is being simultaneously updated by multiple threads
;; **

;; @@
(defn build-dictionary!
  [dict terms]
  (reduce add-term-to-dict! dict terms))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/build-dictionary!</span>","value":"#'cljds.ch6.ws/build-dictionary!"}
;; <=

;; **
;;; To calculate distance we will create a vector from the dictionary and document. In this way we can easily compare term frequencies because they will occupy the same index of the vector
;; **

;; @@
(defn term-id
  [dict term]
  (get-in @dict [:terms term]))

(defn term-frequencies
  [dict terms]
  (->> (map #(term-id dict %) terms)
       (remove nil?)
       (frequencies)))

(defn map->vector
  [dict id-counts]
  (let [zeros (vec (replicate (:count @dict) 0))]
    (-> (reduce #(apply assoc! %1 %2) 
                (transient zeros)
                id-counts)
        (persistent!))))

(defn tf-vector
  [dict document]
  (map->vector dict (term-frequencies dict document)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/tf-vector</span>","value":"#'cljds.ch6.ws/tf-vector"}
;; <=

;; @@
(def dictionary
  (atom {:count 0
         :terms {}}))

(let [doc (reuters-terms "reut2-020.sgm-742.txt")
      dict (build-dictionary! dictionary doc)]
  (println "Document: "   doc)
  (println "Dictionary: " dict)
  (println "Vector: " (tf-vector dict doc)))
;; @@
;; ->
;;; Document:  [nyse s phelan says nyse will continue program trading curb until volume slows]
;;; Dictionary:  #object[clojure.lang.Atom 0x56874a8c {:status :ready, :val {:count 12, :terms {s 1, curb 8, phelan 2, says 3, trading 7, nyse 0, until 9, continue 5, volume 10, will 4, slows 11, program 6}}}]
;;; Vector:  [2 1 1 1 1 1 1 1 1 1 1 1]
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(defn print-distance
  [doc-a doc-b measure]
  (let [a-terms (reuters-terms doc-a)
        b-terms (reuters-terms doc-b)
        dict (-> dictionary
                 (build-dictionary! a-terms)
                 (build-dictionary! b-terms))
        a (tf-vector dict a-terms)
        b (tf-vector dict b-terms)]
    (println "A: " a)
    (println "B: " b)
    (println "Distance: " (measure a b))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/print-distance</span>","value":"#'cljds.ch6.ws/print-distance"}
;; <=

;; @@
(print-distance "reut2-020.sgm-742.txt"
				"reut2-020.sgm-932.txt"
                euclidean-distance)
;; @@
;; ->
;;; A:  [2 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0]
;;; B:  [2 0 1 1 1 0 0 0 0 0 0 0 1 1 1 1 1 1]
;;; Distance:  3.7416573867739413
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Cosine Distance
;; **

;; @@
(defn cosine-similarity
  [a b]
  (let [dot-product (->> (map * a b)
                         (apply +))
        magnitude (fn [d]
                    (->> (map i/sq d)
                         (apply +)
                         (i/sqrt)))]
    (/ dot-product 
       (* (magnitude a) 
          (magnitude b)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/cosine-similarity</span>","value":"#'cljds.ch6.ws/cosine-similarity"}
;; <=

;; @@
(print-distance "reut2-020.sgm-742.txt"
				"reut2-020.sgm-932.txt"
                cosine-similarity)
;; @@
;; ->
;;; A:  [2 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0]
;;; B:  [2 0 1 1 1 0 0 0 0 0 0 0 1 1 1 1 1 1]
;;; Distance:  0.5012804118276031
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Stemming
;; **

;; @@
(defn add-documents-to-dictionary!
  [dict docs]
  (reduce build-dictionary! dict docs))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/add-documents-to-dictionary!</span>","value":"#'cljds.ch6.ws/add-documents-to-dictionary!"}
;; <=

;; @@
(def dictionary
  (atom {:count 0
         :terms {}}))

(let [a (stemmer/stems "music is the food of love")
      b (stemmer/stems "it's lovely that you're musical")]
  (add-documents-to-dictionary! dictionary [a b])
  (cosine-similarity (tf-vector dictionary a)
                     (tf-vector dictionary b)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.8164965809277259</span>","value":"0.8164965809277259"}
;; <=

;; **
;;; # Clustering
;;; 
;;; ## K-means
;; **

;; @@
(defn centroid
  [xs]
  (let [m (i/trans (i/matrix xs))]
    (if (> (i/ncol m) 1)
      (i/matrix (map s/mean m))
      m)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/centroid</span>","value":"#'cljds.ch6.ws/centroid"}
;; <=

;; @@
(let [m (i/matrix [[1 2 3]
                   [2 2 5]])]
  (centroid m))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'> A 3x1 matrix\n -------------\n 1.50e+00 \n 2.00e+00 \n 4.00e+00 \n</span>","value":" A 3x1 matrix\n -------------\n 1.50e+00 \n 2.00e+00 \n 4.00e+00 \n"}
;; <=

;; @@
(defn conj-into [m coll]
  (let [f (fn [m [k v]]
            (update-in m [k] conj v))]
    (reduce f m coll)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/conj-into</span>","value":"#'cljds.ch6.ws/conj-into"}
;; <=

;; @@
(defn clusters
  [cluster-ids data]
  (->> (map vector cluster-ids data)
       (conj-into {})
       vals
       (map i/matrix)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/clusters</span>","value":"#'cljds.ch6.ws/clusters"}
;; <=

;; @@
(let [m (i/matrix [[1 2 3]
                   [4 5 6]
                   [7 8 9]])]
  (clusters [0 1 0] m))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-unkown'> A 2x3 matrix\n -------------\n 7.00e+00  8.00e+00  9.00e+00 \n 1.00e+00  2.00e+00  3.00e+00 \n</span>","value":" A 2x3 matrix\n -------------\n 7.00e+00  8.00e+00  9.00e+00 \n 1.00e+00  2.00e+00  3.00e+00 \n"},{"type":"html","content":"<span class='clj-unkown'> A 1x3 matrix\n -------------\n 4.00e+00  5.00e+00  6.00e+00 \n</span>","value":" A 1x3 matrix\n -------------\n 4.00e+00  5.00e+00  6.00e+00 \n"}],"value":"( A 2x3 matrix\n -------------\n 7.00e+00  8.00e+00  9.00e+00 \n 1.00e+00  2.00e+00  3.00e+00 \n  A 1x3 matrix\n -------------\n 4.00e+00  5.00e+00  6.00e+00 \n)"}
;; <=

;; @@
(defn indices-of [coll value]
  (keep-indexed (fn [idx x]
                  (when (= x value)
                    idx)) 
                coll))

(defn index-of [coll value]
  (first (indices-of coll value)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/index-of</span>","value":"#'cljds.ch6.ws/index-of"}
;; <=

;; @@
(defn k-means [data k]
  (loop [centroids (s/sample data :size k)
         previous-cluster-ids nil]
    (let [cluster-id (fn [x]
                       (let [similarity  #(s/cosine-similarity x %)
                             similarities (map similarity centroids)]
                         (->> (apply max similarities)
                              (index-of similarities))))
          cluster-ids (map cluster-id data)
          clustered (clusters cluster-ids data)]
      (if (not= cluster-ids previous-cluster-ids)
        (recur (map centroid clustered)
               cluster-ids)
clustered))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/k-means</span>","value":"#'cljds.ch6.ws/k-means"}
;; <=

;; @@
(defn too-short? [document]
  (< (count (str document)) 500))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/too-short?</span>","value":"#'cljds.ch6.ws/too-short?"}
;; <=

;; @@
(defn stem-reuters [content]
  (-> (str/replace content  #"^.*\n\n" "")
      (stemmer/stems)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/stem-reuters</span>","value":"#'cljds.ch6.ws/stem-reuters"}
;; <=

;; @@
(defn empty-dict!
  [dict]
  (reset! dict {:count 0
                :terms {}}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/empty-dict!</span>","value":"#'cljds.ch6.ws/empty-dict!"}
;; <=

;; @@
(empty-dict! dictionary)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:count</span>","value":":count"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[:count 0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:terms</span>","value":":terms"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[],"value":"{}"}],"value":"[:terms {}]"}],"value":"{:count 0, :terms {}}"}
;; <=

;; @@
(def test-k-means
(let [docs (mfs/glob "data/reuters-text/*.txt")
      doc-count 100
      k 5
      tokenized (->> (map slurp docs)
                     (remove too-short?)
                     (take doc-count)
                     (map stem-reuters))]
    (add-documents-to-dictionary! dictionary tokenized)
    (-> (map #(tf-vector dictionary %) tokenized)
(k-means k))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/test-k-means</span>","value":"#'cljds.ch6.ws/test-k-means"}
;; <=

;; @@
(defn id->term [dict term-id]
  (some (fn [[term id]]
          (when (= id term-id)
            term))
  (:terms @dict)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/id-&gt;term</span>","value":"#'cljds.ch6.ws/id->term"}
;; <=

;; @@
(defn cluster-summary
  [dict clusters top-term-count]
  (for [cluster clusters]
    (let [sum-terms (if (= (i/nrow cluster) 1)
                      cluster
                      (->>
                        (i/trans cluster)
                        (map s/mean)
                        (i/trans)))
          popular-term-ids (->> 
                             (map-indexed vector sum-terms)
                             (sort-by second >)
                             (take top-term-count)
                             (map first))
          top-terms (map #(id->term dict %) popular-term-ids)]
      (println "N:" (i/nrow cluster))
      (println "Terms:" top-terms))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/cluster-summary</span>","value":"#'cljds.ch6.ws/cluster-summary"}
;; <=

;; @@
(cluster-summary dictionary
                 test-k-means
                 5)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}],"value":"(N: 7\nTerms: (000 dlr mln for ct)\nN: 9\nTerms: (said oil price opec it)\nnil N: 65\nTerms: (said for pct year bank)\nnil N: 17\nTerms: (said offer for would reuter)\nnil N: 2\nTerms: (new nrc plan plant china)\nnil nil)"}
;; <=

;; **
;;; ## Large-scale clustering
;; **

;; @@
(defn text->sequencefile
  [in-path out-path]
  (SequenceFilesFromDirectory/main
    (into-array String (vector "-i" in-path
                               "-o" out-path
                               "-xm" "sequential"
                               "-ow"))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/text-&gt;sequencefile</span>","value":"#'cljds.ch6.ws/text->sequencefile"}
;; <=

;; @@
(text->sequencefile "data/reuters-text"
                    "data/reuters-sequencefile")
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Creating distributed unique IDs
;; **

;; @@
(defn uuid []
  (str (java.util.UUID/randomUUID)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/uuid</span>","value":"#'cljds.ch6.ws/uuid"}
;; <=

;; @@
(uuid)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;e2feffa1-fe0a-46e1-ae49-ba4f5e48e29f&quot;</span>","value":"\"e2feffa1-fe0a-46e1-ae49-ba4f5e48e29f\""}
;; <=

;; **
;;; The issue with using `uuid` is that every worker will generate its own unique ID for every record.
;;; 
;;; One trick would be to hash the word: by hashing it we are guaranteed to get the same result everytime a worker hashes the same word.
;;; 
;;; The only issue is that we might get some clashing, but it should be a small percentage of the whole dataset.
;;; 
;;; ### Distributed IDs with Hadoop
;; **

;; @@
(defn document-count-m
  {::mr/source-as :vals}
  [documents]
  (->> documents
       (r/mapcat (comp distinct stemmer/stems))
	   (r/map #(vector % 1))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/document-count-m</span>","value":"#'cljds.ch6.ws/document-count-m"}
;; <=

;; @@
(defn unique-index-r
  {::mr/source-as :keyvalgroups
   ::mr/sink-as dux/named-keyvals}
  [coll]
  (let [global-offset (conf/get-long mr/*context*
                                     "mapred.task.partition" -1)]
    (tr/mapcat-state
      (fn [local-offset [word doc-counts]]
        [(inc local-offset)
         (if (identical? ::finished word)
           [[:counts [global-offset local-offset]]]
           [[:data [word [[global-offset local-offset]
                          (apply + doc-counts)]]]])])
      0 (r/mapcat identity [coll [[::finished nil]]]))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;cljds.ch6.ws/unique-index-r</span>","value":"#'cljds.ch6.ws/unique-index-r"}
;; <=

;; @@

;; @@
