(ns clojureoids.factory
  (:use clojureoids.model
        clojureoids.logic
        clojureoids.space
        clojureoids.renderer
        clojureoids.random)
  (:import clojureoids.model.world
           clojureoids.model.game-element))

(defn gen-asteroid [radius edges variance]
  (let [polygon (xy-to-polygon (gen-circle-outline radius edges variance))]
    (new game-element
      (gen-asteroid-stats radius)
      #(default-renderer polygon %)
      advance-asteroid)))

(defn gen-player-ship [user-input-atom]
  (let [polygon (gen-ship-polygon)]
    (new game-element
      (gen-ship-stats)
      #(default-renderer polygon %)
      #(advance-ship % user-input-atom))))

(defn gen-demo-world [asteroidcount user-input-atom]
  (let [gen-demo-asteroid #(gen-asteroid (random-int-in-range 5 35) (random-int-in-range 9 35) 0.45)
        game-elements (repeatedly asteroidcount gen-demo-asteroid)
        player-ship (gen-player-ship user-input-atom)]
    (new world (concat game-elements [player-ship]) field-width field-height)))
;(new world [player-ship] field-width field-height)))
;(new world game-elements field-width field-height)))

