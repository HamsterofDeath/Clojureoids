(ns clojureoids.renderer
  (:import [java.awt.geom AffineTransform])
  (:import [java.awt Color Graphics2D])
  (:import [java.awt.geom Area])
  (:use clojureoids.math clojureoids.dependencyfuckup))

(defn adjusted-transform [graphics xy radians])

(defn gen-renderer [game-element]
  ((get game-element :gen-irender ) (:stats game-element)))

(defn get-transform-of
  ([position-accessor rotation-accessor]
    (get-transform-of (new AffineTransform) position-accessor rotation-accessor))
  ([base-transform position-accessor rotation-accessor]
    (let [transform base-transform
          xy (position-accessor)
          rotation-radians (rotation-accessor)
          final-transform
          (-> transform
            (#(adjust-rotation % rotation-radians xy))
            (#(adjust-position % xy)))]
      transform)))

(defn render-area [area image position-accessor rotation-accessor check-warp]
  (let [graphics (.getGraphics image)
        xy (position-accessor)
        rotation-radians (rotation-accessor)
        final-transform (get-transform-of (.getTransform graphics) position-accessor rotation-accessor)]
    (.setColor graphics Color/white)
    (.setTransform graphics final-transform)
    (.draw graphics area)
    (when check-warp
      (let [transformed-area-bounds (.createTransformedShape final-transform (.getBounds2D area))]
        (when (on-left-border? transformed-area-bounds) (render-area area image #(warp-right xy) rotation-accessor false))
        (when (on-right-border? transformed-area-bounds) (render-area area image #(warp-left xy) rotation-accessor false))
        (when (on-top-border? transformed-area-bounds) (render-area area image #(warp-down xy) rotation-accessor false))
        (when (on-bottom-border? transformed-area-bounds) (render-area area image #(warp-up xy) rotation-accessor false))))))


(defn render-world [world target-image]
  (doseq [game-element (:game-elements world)]
    (let [stats (:stats game-element)
          renderer (:gen-irender game-element)]
      (render (renderer stats) target-image))))

(defn area-renderer [area position-accessor rotation-accessor]
  (reify IRender
    (render [this render-target]
      (render-area area render-target position-accessor rotation-accessor true))
    (get-shape [this]
      (let [transform (get-transform-of position-accessor rotation-accessor)]
        (if
          (instance? java.awt.geom.Area area)
          (.createTransformedArea area transform)
          (.createTransformedShape transform area))))
    (get-transformed-bounds [this]
      (let [transform (get-transform-of area position-accessor rotation-accessor)]
        (.createTransformedShape transform (.getBounds2D area))))
    (get-transform [this]
      (get-transform-of position-accessor rotation-accessor))
    (get-reverse-transform [this]
      (.createInverse (get-transform-of position-accessor rotation-accessor)))
    (get-raw-shape [this]
      area)
    (new-renderer [this shape stats]
      (area-renderer shape #(:position stats) #(:rotation-radians stats)))))

(defn default-renderer [area stats]
  (area-renderer area #(:position stats) #(:rotation-radians stats)))

(defn gen-bullet-area []
  (let [polygon (new java.awt.Polygon)]
    (.addPoint polygon 0 0)
    (.addPoint polygon 0 1)
    (.addPoint polygon 1 1)
    (.addPoint polygon 1 0)
    (new Area polygon)))

(defn gen-ship-area []
  (let [polygon (new java.awt.Polygon)]
    (.addPoint polygon 0 8)
    (.addPoint polygon 6 -8)
    (.addPoint polygon 0 0)
    (.addPoint polygon -6 -8)
    (new Area polygon)))

