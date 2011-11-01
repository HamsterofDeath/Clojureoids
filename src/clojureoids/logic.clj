(ns clojureoids.logic
  (:import clojureoids.javainterop.AdvanceCallback)
  (:use clojureoids.model clojureoids.space)
  (:import clojureoids.space.xy))


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

