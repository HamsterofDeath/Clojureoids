(ns clojureoids.logic
  (:import clojureoids.javainterop.AdvanceCallback)
  (:use clojureoids.renderer clojureoids.model)
  (:import clojureoids.model.xy))

(defn apply-warp-to-position [position]
  (let [old-x (:x position)
        old-y (:y position)
        tmp-x (if (< old-x 0) (+ old-x field-width) old-x)
        tmp-y (if (< old-y 0) (+ old-y field-height) old-y)
        new-x (if (> tmp-x field-width) (- tmp-x field-width) tmp-x)
        new-y (if (> tmp-y field-height) (- tmp-y field-height) tmp-y)]
    (if (and (= new-x old-x) (= new-y old-y)) position (new xy new-x new-y))))

(defn apply-warp
  [game-element]
  (let [xy (get-in game-element [:stats :position ])]
    (assoc-in game-element [:stats :position ] (apply-warp-to-position xy))))

(defn advance-movement [game-element]
  (let [stats (:stats game-element)
        position-change (:movement stats)
        x-change (:x position-change)
        y-change (:y position-change)
        with-new-movement
        (assoc-in game-element [:stats :position ] (translated (:position stats) x-change y-change))]
    with-new-movement))

(defn advance-rotation [game-element]
  (let [stats (:stats game-element)
        with-new-rotation
        (assoc-in game-element [:stats :rotation-radians ] (+ (:rotation-radians stats) (:spin stats)))]
    with-new-rotation))

(defn advance-asteroid [asteroid]
  (-> asteroid advance-rotation advance-movement apply-warp))

