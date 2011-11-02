(ns clojureoids.renderer
  (:import [java.awt Color Polygon Graphics2D])
  (:use clojureoids.space))

(defn adjust-rotation [graphics radians anchor]
  (let [transform (.getTransform graphics)]
    (.rotate transform radians (:x anchor) (:y anchor))
    (.setTransform graphics transform)))

(defn render-polygon [polygon image position-accessor rotation-accessor check-warp]
  (let [graphics (.getGraphics image)
        xy (position-accessor)
        rotation-radians (rotation-accessor)]
    (.setColor graphics Color/white)
    (adjust-rotation graphics rotation-radians xy)
    (.translate graphics (int (:x xy)) (int (:y xy)))
    (.draw graphics polygon)
    (when check-warp
      (when (on-left-border? xy polygon) (render-polygon polygon image #(warp-right xy) rotation-accessor false))
      (when (on-right-border? xy polygon) (render-polygon polygon image #(warp-left xy) rotation-accessor false))
      (when (on-top-border? xy polygon) (render-polygon polygon image #(warp-down xy) rotation-accessor false))
      (when (on-bottom-border? xy polygon) (render-polygon polygon image #(warp-up xy) rotation-accessor false)))))

(defprotocol IRender
  (render [this render-target]))

(defn render-world [world target-image]
  (doseq [game-element (:game-elements world)]
    (render ((:gen-irender game-element) (:stats game-element)) target-image)))

(defn polygon-renderer [polygon position-accessor rotation-accessor]
  (reify IRender
    (render [this render-target]
      (render-polygon polygon render-target position-accessor rotation-accessor true))))

(defn default-renderer [polygon stats]
  (polygon-renderer polygon #(:position stats) #(:rotation-radians stats)))

(defn gen-ship-polygon []
  (let [polygon (new Polygon)]
    (.addPoint polygon 0 8)
    (.addPoint polygon 6 -8)
    (.addPoint polygon 0 0)
    (.addPoint polygon -6 -8)
    polygon))
