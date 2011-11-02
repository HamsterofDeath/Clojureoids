(ns clojureoids.space)

(def field-width 800)
(def field-height 600)

(defrecord xy [x y])

(defn on-top-border? [position polygon]
  true)
(defn on-bottom-border? [position polygon]
  true)
(defn on-left-border? [position polygon]
  true)
(defn on-right-border? [position polygon]
  true)

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

(defn apply-warp
  [game-element]
  (let [xy (get-in game-element [:stats :position ])]
    (assoc-in game-element [:stats :position ] (apply-warp-to-position xy))))

