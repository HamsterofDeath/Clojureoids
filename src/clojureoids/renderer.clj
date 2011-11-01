(ns clojureoids.renderer
  (:import [java.awt Color]))

(defn adjust-rotation [graphics radians anchor]
  (let [transform (.getTransform graphics)]
    (.rotate transform radians (:x anchor) (:y anchor))
    (.setTransform graphics transform)))

(defn render-polygon [polygon image position-accessor rotation-accessor]
  (let [graphics (.getGraphics image)
        xy (position-accessor)
        rotation-radians (rotation-accessor)]
    (.setColor graphics Color/white)
    (adjust-rotation graphics rotation-radians xy)
    (.translate graphics (int (:x xy)) (int (:y xy)))
    (.draw graphics polygon)))

(defprotocol IRender
  (render [this render-target]))

(defn render-world [world target-image]
  (doseq [game-element (:game-elements world)]
    (render ((:gen-irender game-element) (:stats game-element)) target-image)))

(defn polygon-renderer [polygon position-accessor rotation-accessor]
  (reify IRender
    (render [this render-target]
      (render-polygon polygon render-target position-accessor rotation-accessor))))

(defn default-renderer [polygon stats]
  (polygon-renderer polygon #(:position stats) #(:rotation-radians stats)))












