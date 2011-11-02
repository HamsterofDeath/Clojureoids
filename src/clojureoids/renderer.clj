(ns clojureoids.renderer
  (:import [java.awt.geom AffineTransform])
  (:import [java.awt Color Graphics2D])
  (:import [java.awt.geom Area])
  (:use clojureoids.math))


(defn adjusted-transform [graphics xy radians])

(defn render-area [area image position-accessor rotation-accessor check-warp]
  (let [graphics (.getGraphics image)
        xy (position-accessor)
        rotation-radians (rotation-accessor)
        final-transform
        (-> (.getTransform graphics)
          (#(adjust-rotation % rotation-radians xy))
          (#(adjust-position % xy)))]
    (.setColor graphics Color/white)
    (.setTransform graphics final-transform)
    (.draw graphics area)
    (when check-warp
      (when (on-left-border? xy area) (render-area area image #(warp-right xy) rotation-accessor false))
      (when (on-right-border? xy area) (render-area area image #(warp-left xy) rotation-accessor false))
      (when (on-top-border? xy area) (render-area area image #(warp-down xy) rotation-accessor false))
      (when (on-bottom-border? xy area) (render-area area image #(warp-up xy) rotation-accessor false)))))

(defprotocol IRender
  (render [this render-target]))

(defn render-world [world target-image]
  (doseq [game-element (:game-elements world)]
    (render ((:gen-irender game-element) (:stats game-element)) target-image)))

(defn area-renderer [area position-accessor rotation-accessor]
  (reify IRender
    (render [this render-target]
      (render-area area render-target position-accessor rotation-accessor true))))

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
