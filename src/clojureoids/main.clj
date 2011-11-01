(ns clojureoids.main
  (:use clojureoids.renderer clojureoids.logic clojureoids.factory clojureoids.model)
  (:import [clojureoids.javainterop MainFrame AdvanceCallback]))

(defn advance-world-and-render [world]
  (let [most-recent-world (atom world)]
    (reify AdvanceCallback
      (onTick [this target-image]
        (render-world @most-recent-world target-image)
        (let [old-world @most-recent-world
              old-game-elements (:game-elements old-world)
              advanced-game-elements
              (for [game-element old-game-elements]
                ((:advance-function game-element) game-element))]
          (swap! most-recent-world #(assoc % :game-elements advanced-game-elements)))))))

(let [world (gen-demo-world 100)
      uiaccess (MainFrame/createFrame field-width field-height)]
  (.initAdvanceCallback uiaccess (advance-world-and-render world)))
