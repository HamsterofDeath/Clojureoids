(ns clojureoids.logic
  (:import clojureoids.javainterop.AdvanceCallback clojureoids.javainterop.UserInput clojureoids.math.xy)
  (:use clojureoids.model clojureoids.math))

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
  (on-tick (:iweapon game-element) game-element))

(defn advance-asteroid [asteroid]
  (-> asteroid
    advance-rotation
    advance-movement
    apply-warp
    advance-weapon))

(defn advance-bullet
  ([bullet remaining-ticks]
    (if (< 0 remaining-ticks)
      (-> bullet
        advance-movement
        apply-warp
        ((fn [old-bullet] (assoc old-bullet :advance-function #(advance-bullet % (- remaining-ticks 1))))))
      []))
  ([bullet]
    (advance-bullet bullet bullet-life-time)))

(defn advance-ship [old-ship user-input-atom]
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
            (fire (:iweapon ship) ship)
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

