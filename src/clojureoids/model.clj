(ns clojureoids.model
  (:import java.lang.Math)
  (:use clojureoids.random))

(def field-width 640)
(def field-height 480)

(defrecord polygon [edges])
(defrecord gameelement [stats polygon-outline])
(defrecord stats [health position movement pointing-at spin])
(defrecord xy [x y])
(defrecord movement [xy slowdown-factor])
(defrecord world [gameelements width height])

(defn gen-circle-outline [radius numPoints max-random-variance-percent]
  (let [radians-inc-per-step (/ (* 2 Math/PI) numPoints)
        polygon-edges-at-radians (map #(* radians-inc-per-step %) (range 0 numPoints))
        radians-to-x (fn [radians] (Math/sin radians))
        radians-to-y (fn [radians] (Math/cos radians))
        radians-to-position (fn [radians] (new xy (radians-to-x radians) (radians-to-y radians)))
        radians-of-edges (map #(* radians-inc-per-step %) (range numPoints))
        multiplied (fn [factor position] (new xy (* factor (:x position)) (* factor (:y position))))
        random-variance-factor
        (fn [] (let [half-max-variance (/ max-random-variance-percent 2)
                     random-factor (* (random-double-smaller-than max-random-variance-percent))]
                 (+ 1 (- (* random-factor max-random-variance-percent) half-max-variance))))]
    (map #(multiplied (* radius (random-variance-factor)) (radians-to-position %)) radians-of-edges)))

(defn gen-asteroid-starting-position []
  ())

(defn gen-asteroid-movement []
  ())

(defn gen-random-direction [] (* 2 random-double Math/PI))

(defn gen-asteroid-stats [radius]
  (new stats
    radius gen-asteroid-starting-position
    (gen-asteroid-movement radius) (gen-random-direction) (gen-random-spin)))

(defn gen-asteroid [radius edges variance]
  (new gameelement (gen-asteroid-stats radius) (gen-circle-outline radius edges variance)))

(defn gen-demo-world [asteroidcount]
  (repeatedly asteroidcount #(gen-asteroid (random-int-in-range 5 25) (random-int-in-range 9 25) 0.3)))


 
