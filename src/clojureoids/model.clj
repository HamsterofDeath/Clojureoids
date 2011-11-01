(ns clojureoids.model
  (:import java.lang.Math)
  (:use clojureoids.random clojureoids.renderer))

(def field-width 800)
(def field-height 600)

(defrecord polygon [edges])
(defrecord stats [health position movement rotation-radians spin])
(defrecord xy [x y])
(defrecord movement [xy slowdown-factor])
(defrecord game-element [stats gen-irender advance-function])
(defrecord world [game-elements width height])

(defn translated [position x y]
  (new xy (+ (:x position) x) (+ (:y position) y)))

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

(defn initally-shift-position [position]
  (translated position (* 0.5 field-width) (* 0.5 field-height)))

(defn gen-asteroid-starting-position []
  (initally-shift-position
    (multiplied
      (/ (min field-height field-width) (+ 1.0 (random-double)) 2)
      (gen-random-normalized-vector))))

(defn gen-asteroid-movement [radius] (multiplied (initial-speed-by-radius radius) (gen-random-normalized-vector)))

(defn gen-random-spin [radius] (* (initial-spin-by-radius radius) (- (random-double) 0.5)))

(defn gen-asteroid-stats [radius]
  (new stats
    radius (gen-asteroid-starting-position)
    (gen-asteroid-movement radius) (gen-random-direction) (gen-random-spin radius)))
