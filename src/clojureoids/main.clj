(ns clojureoids.main
  (:use clojureoids.model clojureoids.renderer)
  (:import [clojureoids.javainterop MainFrame]))

(let [world (gen-demo-world 10)
      uiaccess (MainFrame/createFrame field-width field-height)]
  (render-world-to-panel world uiaccess))
