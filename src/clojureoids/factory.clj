(ns clojureoids.factory
  (:use clojureoids.model
        clojureoids.logic
        clojureoids.math
        clojureoids.dependencyfuckup
        clojureoids.renderer
        clojureoids.random)
  (:import clojureoids.model.world
           clojureoids.model.stats
           clojureoids.model.game-element))

(defn gen-asteroid [radius edges variance]
  (let [area (xy-to-area (gen-circle-outline radius edges variance))]
    (new-peaceful-game-element
      (gen-asteroid-stats radius)
      #(default-renderer area %)
      advance-asteroid)))

(defn gen-bullet [ship]
  (let [area (gen-bullet-area)]
    (new-peaceful-game-element
      (new stats 10 (position-of ship) (multiplied 8 (direction-of ship)) 0 0 bullet-effect)
      #(default-renderer area %)
      #(advance-bullet %1 %2))))

(defn gen-bullet-cannon
  ([]
    (gen-bullet-cannon 14))
  ([heat]
    (reify iweapon
      (fire [this from-game-element]
        (if
          (<= heat 0)
          [(assoc-in from-game-element [:gen-iweapon ] (gen-bullet-cannon 24))
           (gen-bullet from-game-element)]
          [from-game-element]))
      (on-tick [this from-game-element]
        (assoc-in from-game-element [:gen-iweapon ] (gen-bullet-cannon (- heat 1)))))))

(defn gen-player-ship [user-input-atom]
  (let [area (gen-ship-area)]
    (new game-element
      (gen-ship-stats)
      #(default-renderer area %)
      #(advance-ship %1 user-input-atom %2)
      (gen-bullet-cannon))))


(defn gen-demo-world [asteroidcount user-input-atom]
  (let [gen-demo-asteroid #(gen-asteroid (random-int-in-range 5 35) (random-int-in-range 9 35) 0.45)
        game-elements (repeatedly asteroidcount gen-demo-asteroid)
        player-ship (gen-player-ship user-input-atom)]
    (new world (concat game-elements [player-ship]) field-width field-height)))
;(new world [player-ship] field-width field-height)))
;(new world game-elements field-width field-height)))

