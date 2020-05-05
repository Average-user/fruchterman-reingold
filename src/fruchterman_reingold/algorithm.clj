(ns fruchterman-reingold.algorithm
  (:require [clojure.set :as s]))

;; Implementation of Fruchterman-Reingold algorithm for drawing graphs
;; [T. Fruchterman and E. Reingold. Graph drawing by force-directed placement]
;; Here a Graph (G) is a hashmap of the form {:V <set of vertices> :E <set of sets of two vertices>}
;; Look at some examples at graphs.clj


;; Some simple vector operations to make the rest of the code more expressive
(defn- vadd [v u] (mapv + v u))
(defn- vmul [a v] (mapv #(* a %) v))
(defn- vdiv [v a] (vmul (/ 1 a) v))
(defn- norm [v] (Math/sqrt (reduce + (map #(Math/pow % 2) v))))

;; ================= Algorithm itself =================
(defn- initialize-graph
  "Initialize vertices at random positions, returns a hashmap
  with vertices as keys and coordinates (vectors) as values"
  [G w h]
  (into {} (map (fn [v] [v [(rand w) (rand h)]]) (:V G))))

(defn initial-state
  "The state keeps record of needed information, but actually
  the only data that is not constant during an execution is
  :t and :pos"
  [G w h C]
  (let [area (* w h)
        k    (* C (Math/sqrt (/ area (count (:V G)))))]
    {:G    (update G :E #(mapcat (fn [s] (let [[x y] (vec s)] [[x y] [y x]])) %))
     ;; The algorithm uses ordered pairs. We make the change internal to ensure
     ;; the input to be tought as undirected graph
     :W    w
     :H    h
     :pos  (initialize-graph G w h)
     :t    (/ w 10)
     :fr   (fn [x] (/ (Math/pow k 2) x))
     :fa   (fn [x] (/ (Math/pow x 2) k))
     :cool (fn [t] (* 0.99 t))}))

(defn- get-repulsive-disp
  "Calculate the total repulsive vector force on a single vertex v"
  [V pos fr v]
  (let [f (fn [dispv u] (let [dif (vadd (pos v) (vmul -1 (pos u)))
                              d   (norm dif)]
                          (vadd dispv (vmul (fr d) (vdiv dif d)))))]
    (reduce f [0 0] (s/difference V #{v}))))

(defn- calculate-repulsive-forces
  "Creates disp hash-map of vertex to vector displacement"
  [V pos fr]
  (let [f (fn [disp v] (assoc disp v (get-repulsive-disp V pos fr v)))]
    (reduce f {} V)))

(defn- calculate-attractive-forces
  "Updates displacements vectors wiht attractive forces
  of connected pairs. (Edges)"
  [E pos fa disp]
  (let [f (fn [disp [v u]] (let [dif    (vadd (pos v) (vmul -1 (pos u)))
                                 d      (norm dif)
                                 change (vmul (fa d) (vdiv dif d))]
                             (-> disp (update v #(vadd % (vmul -1 change)))
                                      (update u #(vadd % change)))))]
    (reduce f disp E)))

(defn- calculate-new-positions
  "Calculates new position with the displacement vectors, but also
  respecting width and height of screen, and temperature 't'"
  [W H t disp pos]
  (let [f (fn [pos v p]
            (let [dis     (disp v)
                  d       (norm dis)
                  [x' y'] (vadd p (vmul (min d t) (vdiv dis d)))]
              (assoc pos v [(min W (max 0 x')) (min H (max 0 y'))])))]
    (reduce-kv f {} pos)))

(defn fruchterman-reingold-step
  "Basically one iteration of the algorithm described by Fruchterman and Reingold,
  the only difference is that here the origin (0,0) is thought in the upper
  left corner and not in the middle"
  [{:keys [G pos fr fa W H cool t] :as state}]
  (let [disp (->> (calculate-repulsive-forces (:V G) pos fr)
                  (calculate-attractive-forces (:E G) pos fa))]
    (-> state
        (update :pos (partial calculate-new-positions W H t disp))
        (update :t cool))))

(defn fruchterman-reingold [G W H C]
  (iterate fruchterman-reingold-step (initial-state G W H C)))
