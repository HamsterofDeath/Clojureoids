(ns clojureoids.renderer
  (:import [java.awt Color]))

(defn render-polygon [polygon image position-accessor]
  (let [graphics (.getGraphics image)
        xy (position-accessor)]
    (.translate graphics (* 0.5 (.getWidth image)) (* 0.5 (.getHeight image)))
    (.setColor graphics Color/white)
    (.translate graphics (int (:x xy)) (int (:y xy)))
    (.draw graphics polygon)))

(defprotocol IRender
  (render [this render-target]))

(defn render-world [world target-image]
  (doseq [game-element (:game-elements world)]
    (render (:irender game-element) target-image)))

(defn render-world-to-panel [world uiaccess]
  (let [render-target (.provideCleanRenderTarget uiaccess)]
    (render-world world render-target)
    (.afterRenderingFinished uiaccess))
  )

(defn polygon-renderer [polygon position-accessor]
  (reify IRender
    (render [this render-target]
      (render-polygon polygon render-target position-accessor))))








