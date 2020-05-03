(ns fruchterman-reingold.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [fruchterman-reingold.algorithm :as al]
            [fruchterman-reingold.graphs :as g]))

(def W 600)
(def H 600)
(def line-weight 3)
(def node-radius 15)

(defn setup []
  (q/ellipse-mode :center)
  (q/frame-rate 60)
  {:states (al/fruchterman-reingold g/Durer (- W 30) (- H 30) 0.7)
   ;; We need to make with and height a little smaller since in the actual drawing
   ;; points have area i.e are not really points
   :i -50})

(defn update-state
  "We make the draw to not update for a while in
  order to look at the initial configuration before it
  starts moving"
  [state]
  (if (neg? (:i state))
    (update state :i inc)
    (-> state
         (update :states next)
         (update :i inc))))
  
(defn draw-state [state]
  (let [{:keys [G pos]} (first (:states state))]
    (q/background 22)
    (q/stroke-weight line-weight)
    (q/stroke 245 0 0)
    (doall (map (fn [e] (let [[a b] (vec e)]
                          (apply q/line (map #(al/vadd [15 15] %) [(pos a) (pos b)]))))
                (:E G)))
    (q/fill 22)
    (q/stroke-weight line-weight)
    (q/stroke 255 0 0)
    (doall (map (fn [[x y]] (q/ellipse (+ x 15) (+ y 15) node-radius node-radius))
                (vals pos)))))

(q/defsketch fruchterman-reingold
  :title "Fruchterman-Reingold Algorithm"
  :size [W H]
  :setup setup
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  :middleware [m/fun-mode])
