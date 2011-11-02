(ns clojureoids.math)

(def field-width 800)
(def field-height 600)

(defrecord xy [x y])


(defn on-top-border? [position area]
  true)
(defn on-bottom-border? [position area]
  true)
(defn on-left-border? [position area]
  true)
(defn on-right-border? [position area]
  true)

(defn adjust-rotation [transform radians anchor]
  (.rotate transform radians (:x anchor) (:y anchor))
  transform)

(defn adjust-position [transform xy]
  (.translate transform (:x xy) (:y xy))
  transform)

(defn translated
  ([position xy]
    (translated position (:x xy) (:y xy)))
  ([position x y]
    (new xy (+ (:x position) x) (+ (:y position) y))))

(defn warp-left [position]
  (translated position (- field-width) 0))

(defn warp-up [position]
  (translated position 0 (- field-height)))

(defn warp-right [position]
  (translated position field-width 0))

(defn warp-down [position]
  (translated position 0 field-height))

(defn apply-warp-to-position [position]
  (let [old-x (:x position)
        old-y (:y position)
        tmp-x (if (< old-x 0) (+ old-x field-width) old-x)
        tmp-y (if (< old-y 0) (+ old-y field-height) old-y)
        new-x (if (> tmp-x field-width) (- tmp-x field-width) tmp-x)
        new-y (if (> tmp-y field-height) (- tmp-y field-height) tmp-y)]
    (if (and (= new-x old-x) (= new-y old-y)) position (new xy new-x new-y))))

(defn apply-warp [game-element]
  (update-in game-element [:stats :position ] #(apply-warp-to-position %)))

(defn length-of [xy]
  (let [x (:x xy)
        y (:y xy)]
    (Math/sqrt (+ (* x x) (* y y)))))

(defn with-length [xy new-length]
  (let [length (length-of xy)]
    (new xy (* new-length (/ (:x xy) length)) (* new-length (/ (:y xy) length)))))

(defn radians-to-x [radians] (- (Math/sin radians)))

(defn radians-to-y [radians] (Math/cos radians))

(defn radians-to-position [radians] (new xy (radians-to-x radians) (radians-to-y radians)))
