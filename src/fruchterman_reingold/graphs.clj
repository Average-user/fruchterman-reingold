(ns fruchterman-reingold.graphs
  (:require [clojure.set :as s]
            [clojure.data.generators :as g]))


;; Some useful random stuff
(defn rand-seed [] (g/int))

(defn randfn
  ([] (randfn (java.util.Random.)))
  ([r] #(.nextDouble r)))

(defn rand-prob-sequence
  "This creates the same probability sequence when
  provided with the same seed"
  [seed]
  (let [source (randfn (java.util.Random. seed))]
    (repeatedly #(source))))

;; Diferent Graphs
(defn windmill-gen [n]
  (if (even? n)
    (windmill-gen (inc n))
    (let [vs (range 1 n)]
      {:V (conj (set vs) 0)
       :E
       (set (concat (map (fn [x] #{0 x}) vs)
                    (map set (partition 2 vs))))})))

(def Heawood
  {:V (set (range 14))
   :E (set (concat (map set (partition 2 1 (range 14)))
                   (list #{13 0})
                   (map (fn [i] #{i (if (even? i) (mod (+ i 5) 13) (mod (+ i 9) 13))})
                        (range 14))))})
           

(def K33 {:V #{:a :b :c :x :y :z}
          :E #{#{:a :x} #{:a :y} #{:a :z}
               #{:b :x} #{:b :y} #{:b :z}
               #{:c :x} #{:c :y} #{:c :z}}})
                
(defn K
  "K_n i shorthand for the complete graph of n vertices"
  [n]
  {:V (set (range n))
   :E (set (for [x (range n), y (range (inc x) n)]
             #{x y}))})

(def Ex {:V #{:a :b :c :d :e :f :g :h}
         :E #{#{:a :b} #{:b :c} #{:c :e} #{:e :f} #{:g :b}
              #{:g :d} #{:b :d} #{:c :d} #{:e :d} #{:f :d} #{:h :d}}})

(def Durer
  {:V #{:a :b :c :d :e :f :x :y :z :h :i :j}
   :E #{#{:a :d} #{:b :e} #{:c :f} #{:d :e} #{:e :f} #{:f :d}
        #{:x :h} #{:y :i} #{:z :j} #{:h :i} #{:i :j} #{:j :h}
        #{:a :y} #{:y :b} #{:b :z} #{:z :c} #{:c :x} #{:x :a}}})

(defn random [seed n p]
  {:V (set (range n))
   :E (->> (for [x (range n), y (range (inc x) n)] #{x y})
        (zipmap (rand-prob-sequence seed))
        (filter (fn [[p' _]] (< p' p)))
        (map second)
        (set))})

(defn prism [n]
  {:V (set (concat (range 1 (inc n))
                   (range (- n) 0)))
   :E (set (concat (map (fn [x] #{x (- x)}) (range 1 (inc n)))
                   (map set (partition 2 1 (range 1 (inc n))))
                   (list #{n 1})
                   (map set (partition 2 1 (reverse (range (- n) 0))))
                   (list #{(- n) -1})))})

(def Triangulated
  {:V #{:a :b :c :d :e :f :g :h :i :j}
   :E #{#{:a :b} #{:a :c}
        #{:b :c} #{:b :d} #{:b :e} #{:c :e} #{:c :f}
        #{:d :e} #{:e :f} #{:d :g} #{:d :h} #{:e :h} #{:e :i} #{:f :i} #{:f :j}
        #{:g :h} #{:h :i} #{:i :j}}})

(defn twin-k
  "Two K_n graphs, joined by a single edge"
  [n]
  {:V (set (range (* 2 n)))
   :E (set (concat (list #{0 n})
                   (for [x (range n), y (range (inc x) n)] #{x y})
                   (for [x (range n (* 2 n)), y (range (inc x) (* 2 n))] #{x y})))})
