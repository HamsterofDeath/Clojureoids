(ns clojureoids.model
  (:import java.lang.Math)
  (:use clojureoids.random clojureoids.helpers clojureoids.renderer clojureoids.logic))

(def field-width 640)
(def field-height 480)

(defrecord polygon [edges])
(defrecord game-element [stats irender advance-function])
(defrecord stats [health position movement pointing-at spin])
(defrecord xy [x y])
(defrecord movement [xy slowdown-factor])
(defrecord world [game-elements width height])

(defn radians-to-x [radians] (Math/sin radians))

(defn radians-to-y [radians] (Math/cos radians))

(defn radians-to-position [radians] (new xy (radians-to-x radians) (radians-to-y radians)))

(defn multiplied [factor position] (new xy (* factor (:x position)) (* factor (:y position))))

(defn gen-circle-outline [radius numPoints max-random-variance-percent]
  (let [radians-inc-per-step (/ (* 2 Math/PI) numPoints)
        polygon-edges-at-radians (map #(* radians-inc-per-step %) (range 0 numPoints))
        radians-of-edges (map #(* radians-inc-per-step %) (range numPoints))
        random-variance-factor
        (fn [] (let [half-max-variance (/ max-random-variance-percent 2)
                     random-factor (* (random-double-smaller-than max-random-variance-percent))]
                 (+ 1 (- (* random-factor max-random-variance-percent) half-max-variance))))]
    (map #(multiplied (* radius (random-variance-factor)) (radians-to-position %)) radians-of-edges)))

(defn xy-to-polygon [xys]
  (let [polygon (new java.awt.Polygon)]
    (doall (for [point xys] [(.addPoint polygon (:x point) (:y point))]))
    polygon))

(defn initial-speed-by-radius [radius]
  (/ radius))

(defn initial-spin-by-radius [radius]
  (/ radius))

(defn gen-random-direction [] (* 2 (num (random-double)) Math/PI))

(defn gen-random-normalized-vector [] (radians-to-position (gen-random-direction)))

(defn gen-asteroid-starting-position []
  (multiplied (/ (min field-height field-width) 2.0) (gen-random-normalized-vector)))

(defn gen-asteroid-movement [radius] (multiplied (initial-speed-by-radius radius) (gen-random-normalized-vector)))

(defn gen-random-spin [radius] (* (initial-spin-by-radius radius) (- (random-double) 0.5)))

(defn gen-asteroid-stats [radius]
  (new stats
    radius (gen-asteroid-starting-position)
    (gen-asteroid-movement radius) (gen-random-direction) (gen-random-spin radius)))

(defn gen-asteroid [radius edges variance]
  (let [stats (gen-asteroid-stats radius)]
    (new game-element
      stats
      (polygon-renderer
        (xy-to-polygon (gen-circle-outline radius edges variance))
        #(:position stats))
      advance-asteroid)))


(defn gen-demo-world [asteroidcount]
  (let [game-elements (repeatedly asteroidcount #(gen-asteroid (random-int-in-range 5 25) (random-int-in-range 9 25) 0.3))]
    (new world game-elements field-width field-height)))
