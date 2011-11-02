(ns clojureoids.main
  (:use clojureoids.renderer clojureoids.logic clojureoids.factory clojureoids.space)
  (:import [clojureoids.javainterop MainFrame AdvanceCallback UIAccess]))

(defn advance-world-and-render [world user-input-atom uiaccess]
  (let [most-recent-world (atom world)]
    (reify AdvanceCallback
      (onTick [this target-image]
        (swap! user-input-atom (fn [discard] (.getMostRecentUserInput uiaccess)))
        (render-world @most-recent-world target-image)
        (let [old-world @most-recent-world
              old-game-elements (:game-elements old-world)
              advanced-game-elements
              (for [game-element old-game-elements]
                ((:advance-function game-element) game-element))]
          (swap! most-recent-world #(assoc % :game-elements advanced-game-elements)))))))

(let [user-input-atom (atom [])
      world (gen-demo-world 100 user-input-atom)
      uiaccess (MainFrame/createFrame field-width field-height)]
  (.initAdvanceCallback uiaccess (advance-world-and-render world user-input-atom uiaccess)))
