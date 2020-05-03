(ns fruchterman-reingold.algorithm
  (:require [clojure.set :as s]))

;; Implementation of Fruchterman-Reingold algorithm for drawing graphs
;; [T. Fruchterman and E. Reingold. Graph drawing by force-directed placement]
;; Here a Graph (G) is a hashmap of the form {:V <set of vertices> :E <set of sets of two vertices>}
;; Look at some examples at graphs.clj


;; Some simple vector operations
(defn vadd [v u]
  (mapv + v u))

(defn vmul [a v]
  (mapv #(* a %) v))

(defn norm [v]
  (Math/sqrt (reduce + (map #(Math/pow % 2) v))))

(defn normalize [v]
  (vmul (/ 1 (norm v)) v))

;; Algorithm itself
(defn initialize-graph
  "Initialize vertices at random positions, returns a hashmap
  with vertices as keys and coordinates (vectors) as values"
  [G w h]
  (let [randx (fn [] (rand w))
        randy (fn [] (rand h))]
    (into {} (map (fn [v] [v [(randx) (randy)]]) (:V G)))))

(defn initial-state
  "The state keeps record of needed information, but actually
  the only data that is not constant during an execution is
  :t and :pos"
  [G w h]
  (let [area (* w h)
        C    0.7                                        ; you should play with this
        k    (* C (Math/sqrt (/ area (count (:V G)))))]
    {:G   G
     :W   w
     :H   h
     :pos (initialize-graph G w h)
     :t   (/ w 10)
     :fr  (fn [x] (/ (Math/pow k 2) x))
     :fa  (fn [x] (/ (Math/pow x 2) k))
     :cool (fn [t] (* 0.99 t))}))

(defn get-repulsive-disp
  "Calculate the total repulsive vector force on a single vertex v"
  [G pos fr v]
  (reduce (fn [disp u] (let [dif (vadd (pos v) (vmul -1 (pos u)))
                             d   (norm dif)]
                         (vadd disp (vmul (fr d) (normalize dif)))))
          [0 0]
          (s/difference (:V G) #{v})))

(defn calculate-repulsive-forces
  "Creates disp hash-map of vertex to vector displacement"
  [G pos fr]
  (into {} (map (fn [v]  [v (get-repulsive-disp G pos fr v)]) (:V G))))

(defn calculate-attractive-forces
  "Updates displacements vectors wiht the attractive forces
  of connected vertices"
  [G pos fa disp]
  (reduce (fn [disp [v u]]
            (let [dif    (vadd (pos v) (vmul -1 (pos u)))
                  d      (norm dif)
                  change (vmul (fa d) (normalize dif))]
              (-> disp (update v #(vadd % (vmul -1 change)))
                       (update u #(vadd % change)))))
          disp
          (mapcat (fn [s] (let [[x y] (vec s)] [[x y] [y x]])) (:E G))))

(defn calculate-new-positions
  "Calculates new position with the displacement vectors, but also
  respecting with and height of screen, and temperature 't'"
  [G W H t disp pos]
  (->> pos
       (map (fn [[v p]] (let [dis     (disp v)
                              normd   (normalize dis)
                              [x' y'] (vadd p (vmul (min (norm dis) t) normd))]
                          [v [(min W (max 0 x'))
                              (min H (max 0 y'))]])))
       (into {})))

(defn iteration
  "Basically one iteration of the algorithm described by Fruchterman and Reingold,
  the only difference is that here the origin (0,0) is thought in the upper
  left corner and not in the middle"
  [{:keys [G pos fr fa W H cool t] :as state}]
  (let [disp (calculate-repulsive-forces G pos fr)
        disp' (calculate-attractive-forces G pos fa disp)]
    (-> state
        (update :pos (partial calculate-new-positions G W H t disp'))
        (update :t cool))))

(defn fruchterman-reingold [G W H]
  (iterate iteration (initial-state G W H)))
