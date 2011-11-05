(ns clojureoids.logic
  (:import clojureoids.javainterop.AdvanceCallback clojureoids.javainterop.UserInput clojureoids.math.xy java.awt.geom.Rectangle2D
           java.awt.geom.AffineTransform)
  (:use clojureoids.model clojureoids.math clojureoids.random clojureoids.dependencyfuckup))

(defn all-relevant-transforms-for-collision-check [game-element]
  (let [zerozero (new xy 0 0)
        transformed-bounds (bounds-of-transformed-shape-of game-element)
        default-transform (new AffineTransform)
        transformation-appender
        (fn [coll pred warp]
          (if
            (pred transformed-bounds)
            (conj coll (transform-with-new-translation default-transform (warp zerozero)))
            coll))
        relevant-transforms
        (-> [default-transform]
          (transformation-appender on-left-border? warp-right)
          (transformation-appender on-right-border? warp-left)
          (transformation-appender on-top-border? warp-down)
          (transformation-appender on-bottom-border? warp-up))]
    relevant-transforms))

(defn warped-shapes-of [game-element]
  (let [shape (transformed-shape-of game-element)]
    (map
      #(.createTransformedShape % shape)
      (all-relevant-transforms-for-collision-check game-element))))

(defn advance-movement [game-element]
  (let [stats (:stats game-element)
        position-change (:movement stats)
        x-change (:x position-change)
        y-change (:y position-change)
        with-new-movement
        (update-in game-element [:stats :position ] #(translated % x-change y-change))]
    with-new-movement))

(defn advance-rotation [game-element]
  (let [stats (:stats game-element)
        with-new-rotation
        (update-in game-element [:stats :rotation-radians ] #(+ % (:spin stats)))]
    with-new-rotation))

(defn advance-weapon [game-element]
  (on-tick (:gen-iweapon game-element) game-element))


(defn with-resettet-rotation [game-element]
  (assoc-in game-element [:stats :rotation-radians ] 0.0))

(defn split-up-asteroid [asteroid bullet]
  (let [broken-off (gen-circle-outline (power-of bullet) (random-int-in-range 5 15) 0.35)
        warped-asteroid-areas (warped-shapes-of asteroid)
        shapes-by-distance (sort-by #(approximate-distance % (bounds-of-transformed-shape-of bullet)) warped-asteroid-areas)
        nearest-shape (first shapes-by-distance)]
    [(with-resettet-rotation
       (update-in
         asteroid [:gen-irender ]
         #(fn [stats] (new-renderer (% stats) (translate-to-origin nearest-shape) stats))))]))

(defn split-up-asteroids [asteroids bullet]
  (flatten (map #(split-up-asteroid % bullet) asteroids)))

(defn apply-collision-of-bullet [bullet world]
  (let [bullet-bounds (bounds-of-transformed-shape-of bullet)
        type-pred
        (fn [check-type]
          (true? (get-in check-type [:stats :effects :affected-by-bullet ])))
        contact-pred
        (fn [asteroid]
          (some
            (fn [shape]
              (.intersects shape bullet-bounds))
            (warped-shapes-of asteroid)))
        full-test (every-pred type-pred contact-pred)
        touched-asteroids (filter full-test (:game-elements world))]
    (if
      (empty? touched-asteroids)
      []
      (split-up-asteroids touched-asteroids bullet))))

(defn apply-collision-of-asteroid [asteroid world]
  (let [asteroid-shapes (warped-shapes-of asteroid)
        type-pred
        (fn [check-type]
          (true? (get-in check-type [:stats :effects :damages-asteroid ])))
        contact-pred
        (fn [bullet]
          (some
            (fn [shape]
              (.intersects shape (bounds-of-transformed-shape-of bullet)))
            asteroid-shapes))
        full-test (every-pred type-pred contact-pred)
        touched-bullets (some full-test (:game-elements world))]
    (if touched-bullets [] [asteroid])))

(defn advance-asteroid [asteroid world]
  (let [collision-result (apply-collision-of-asteroid asteroid world)]
    (if
      (empty? collision-result)
      collision-result
      (-> asteroid
        advance-rotation
        advance-movement
        apply-warp)))
  )

(defn advance-bullet
  ([bullet remaining-ticks world]
    (if (< 0 remaining-ticks)
      (let [collision-result (apply-collision-of-bullet bullet world)]
        (if
          (empty? collision-result)
          (-> bullet
            advance-movement
            apply-warp
            ((fn [old-bullet] (assoc old-bullet :advance-function #(advance-bullet %1 (- remaining-ticks 1) %2)))))
          collision-result))
      []))
  ([bullet world]
    (advance-bullet bullet bullet-life-time world)))

(defn advance-ship [old-ship user-input-atom world]
  (let [user-input @user-input-atom
        apply-max-spin
        (fn [ship]
          (let [current-spin (get-in ship [:stats :spin ])]
            (if
              (>= (Math/abs current-spin) ship-max-spin-speed)
              (assoc-in ship [:stats :spin ] (* (Math/signum current-spin) ship-max-spin-speed))
              ship)))
        apply-max-speed
        (fn [ship]
          (let [current-speed (length-of (get-in ship [:stats :movement ]))]
            (if
              (>= current-speed ship-max-speed)
              (update-in ship [:stats :movement ] #(with-length % ship-max-speed))
              ship)))
        apply-spin-slowdown
        (fn [ship]
          (if (.isLeftOrRight user-input) ship (update-in ship [:stats :spin ] #(* % 0.85))))
        apply-movement-slowdown
        (fn [ship]
          (update-in ship [:stats :movement ] #(multiplied 0.97 %)))
        apply-left
        (fn [ship user-input]
          (if
            (.isLeft user-input)
            (update-in ship [:stats :spin ] #(- % ship-spin-speed))
            ship))
        apply-accelerate
        (fn [ship user-input]
          (if
            (.isAccelerate user-input)
            (let [direction (direction-of ship)
                  add-to-movement (multiplied ship-acceleration direction)]
              (update-in ship [:stats :movement ] #(translated % add-to-movement)))
            ship))
        apply-reverse
        (fn [ship user-input]
          (if
            (.isReverse user-input)
            (let [direction (direction-of ship)
                  add-to-movement (multiplied (- ship-reverse-acceleration) direction)]
              (update-in ship [:stats :movement ] #(translated % add-to-movement)))
            ship))
        apply-right
        (fn [ship user-input]
          (if (.isRight user-input)
            (update-in ship [:stats :spin ] #(+ % ship-spin-speed))
            ship))
        apply-user-movement-input
        (fn [ship]
          (-> ship
            (#(apply-left % user-input))
            (#(apply-right % user-input))
            (#(apply-reverse % user-input))
            (#(apply-accelerate % user-input))))
        apply-attack
        (fn [ship]
          (if
            (.isFire user-input)
            (fire (:gen-iweapon ship) ship)
            [ship]))]
    (-> old-ship
      apply-spin-slowdown
      apply-movement-slowdown
      apply-max-spin
      apply-max-speed
      apply-user-movement-input
      advance-rotation
      advance-movement
      apply-warp
      advance-weapon
      apply-attack)))

