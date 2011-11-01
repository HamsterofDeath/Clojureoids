(ns clojureoids.factory
  (:use clojureoids.model
        clojureoids.logic
        clojureoids.space
        clojureoids.renderer
        clojureoids.random)
  (:import clojureoids.model.world
           clojureoids.model.game-element))

(defn gen-asteroid [radius edges variance]
  (let [stats (gen-asteroid-stats radius)
        polygon (xy-to-polygon (gen-circle-outline radius edges variance))]
    (new game-element
      stats
      #(default-renderer polygon %)
      advance-asteroid)))

(defn gen-demo-world [asteroidcount]
  (let [gen-demo-asteroid #(gen-asteroid (random-int-in-range 5 35) (random-int-in-range 9 35) 0.45)
        game-elements (repeatedly asteroidcount gen-demo-asteroid)]
    (new world game-elements field-width field-height)))

