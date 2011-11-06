(ns clojureoids.model
  (:import [java.awt.geom Area])
  (:import java.lang.Math clojureoids.math.xy java.awt.geom.Area)
  (:use clojureoids.random clojureoids.renderer clojureoids.math clojureoids.dependencyfuckup))

(def ship-max-spin-speed 0.15)
(def ship-spin-speed (/ Math/PI 270.0))
(def ship-max-speed 8.0)
(def ship-acceleration 0.15)
(def ship-reverse-acceleration (/ ship-acceleration 2))
(def bullet-life-time 45)

(defrecord effects [affected-by-bullet damages-asteroid])
(defrecord stats [power position movement rotation-radians spin effects])
(defrecord movement [xy slowdown-factor])
(defrecord game-element [stats gen-irender advance-function gen-iweapon])
(defrecord world [game-elements width height])

(defn power-of [game-element]
  (get-in game-element [:stats :power ]))

(def ship-effect (new effects false false))
(def bullet-effect (new effects false true))
(def asteroid-effect (new effects true false))

(defprotocol iweapon
  (fire [this from-game-element])
  (on-tick [this from-game-element]))

(defn position-of [game-element]
  (get-in game-element [:stats :position ]))
(defn rotation-of [game-element]
  (get-in game-element [:stats :rotation-radians ]))

(def no-weapon
  (reify iweapon
    (fire [this from-game-element]
      [from-game-element])
    (on-tick [this from-game-element]
      from-game-element)))

(defn new-peaceful-game-element [stats gen-irender advance-function]
  (new game-element stats gen-irender advance-function no-weapon))

(defn direction-of [game-element]
  (radians-to-position (get-in game-element [:stats :rotation-radians ])))

(defn speed-of [game-element]
  (length-of (get-in game-element [:stats :movement ])))

(defn multiplied [factor position] (new xy (* factor (:x position)) (* factor (:y position))))

(defn gen-circle-outline [radius numPoints max-random-variance-percent]
  (let [radians-inc-per-step (/ (* 2 Math/PI) numPoints)
        area-edges-at-radians (map #(* radians-inc-per-step %) (range 0 numPoints))
        radians-of-edges (map #(* radians-inc-per-step %) (range numPoints))
        random-variance-factor
        (fn [] (let [half-max-variance (/ max-random-variance-percent 2)
                     random-factor (* (random-double-smaller-than max-random-variance-percent))]
                 (+ 1 (- (* random-factor max-random-variance-percent) half-max-variance))))]
    (map #(multiplied (* radius (random-variance-factor)) (radians-to-position %)) radians-of-edges)))

(defn xy-to-area [xys]
  (let [polygon (new java.awt.Polygon)]
    (doall (for [point xys] [(.addPoint polygon (:x point) (:y point))]))
    (new Area polygon)))

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
    (gen-asteroid-movement radius) (gen-random-direction) (gen-random-spin radius) asteroid-effect))

(defn gen-ship-stats []
  (new stats
    100 (initally-shift-position (new xy 0.0 0.0)) (new xy 0.0 0.0) 0.0 0.0 ship-effect))

(defn transformed-shape-of [game-element]
  (get-shape (gen-renderer game-element)))

(defn approximate-bounds-of [game-element]
  (.getBounds2D (get-transformed-bounds (gen-renderer game-element))))

(defn bounds-of-transformed-shape-of [game-element]
  (.getBounds2D (transformed-shape-of game-element)))

(defn default-transform-of [game-element]
  (get-transform (gen-renderer game-element)))

(defn reverse-transform-of [game-element]
  (get-reverse-transform (gen-renderer game-element)))

(defn raw-shape-of [game-element]
  (get-raw-shape (gen-renderer game-element)))

