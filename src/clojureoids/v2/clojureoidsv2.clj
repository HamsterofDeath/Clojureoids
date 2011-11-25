(ns clojureoids.v2.clojureoidsv2
  (:use clojureoids.v2.Random)
  (:import [java.awt.geom Rectangle2D AffineTransform Area]
           [java.awt.Graphics2D]
           [java.awt.Image]
           [clojureoids.javainterop TransformUtils]
           [clojureoids.javainterop MainFrame]
           [clojureoids.javainterop UIAccess]
           [clojureoids.javainterop AdvanceCallback]
           [clojureoids.javainterop UserInput])
  )

;global constants
(def field-width 800)
(def field-height 600)
(def max-spin-speed 0.15)
(def spin-acc 0.03)
(def forward-acc 0.3)
(def backward-acc (/ forward-acc 2))
(def max-forward-speed 9)
(def max-backward-speed (/ max-forward-speed 2))
(def left-border-line (TransformUtils/newRect 0 0 1 field-height))
(def right-border-line (TransformUtils/newRect (- field-width 1) 0 field-width field-height))
(def top-border-line (TransformUtils/newRect 0 0 field-width 1))
(def bottom-border-line (TransformUtils/newRect 0 (- field-height 1) field-width field-height))
(def left-warp-transform (AffineTransform/getTranslateInstance (- field-width) 0))
(def right-warp-transform (AffineTransform/getTranslateInstance field-width 0))
(def up-warp-transform (AffineTransform/getTranslateInstance 0 (- field-height)))
(def down-warp-transform (AffineTransform/getTranslateInstance 0 field-height))


;java interop
(defn destructured [user-input]
  {:left? (.isLeft user-input)
   :right? (.isRight user-input)
   :thrust? (.isAccelerate user-input)
   :attack? (.isFire user-input)
   :break? (.isReverse user-input)
   })

;basic math stuff
(defrecord xy [x y])

(defn multiplied [factor {:keys [x y]}] (new xy (* factor x) (* factor y)))

(defn center-of [rect]
  (new xy (.getCenterX rect) (.getCenterY rect)))

(defn center-of-shape [shape]
  (center-of (.getBounds2D shape)))

(defn difference-between [from to]
  (new xy (- (:x to) (:x from)) (- (:y to) (:y from))))

(defn length-of
  ([{:keys [x y]}]
    (length-of x y))
  ([x y]
    (Math/sqrt (+ (* x x) (* y y)))))

(defn with-length [new-length {:keys [x y]}]
  (let [length (length-of x y)]
    (if
      (= length 0.0)
      (new xy 0 0)
      (new xy (* new-length (/ x length)) (* new-length (/ y length))))))

(defn radians-to-x [radians] (- (Math/sin radians)))

(defn radians-to-y [radians] (Math/cos radians))

(defn radians-to-xy [radians] (new xy (radians-to-x radians) (radians-to-y radians)))

(defn neg [{:keys [x y]}]
  (new xy (- x) (- y)))

(defn translated
  ([old-xy xy]
    (translated old-xy (:x xy) (:y xy)))
  ([old-xy x y]
    (new xy (+ (:x old-xy) x) (+ (:y old-xy) y))))

(defn gen-circle-outline [radius numPoints max-random-variance-percent]
  (let [radians-inc-per-step (/ (* 2 Math/PI) numPoints)
        area-edges-at-radians (map #(* radians-inc-per-step %) (range 0 numPoints))
        radians-of-edges (map #(* radians-inc-per-step %) (range numPoints))
        random-variance-factor
        (fn [] (let [half-max-variance (/ max-random-variance-percent 2)
                     random-factor (* (random-double-smaller-than max-random-variance-percent))]
                 (+ 1 (- (* random-factor max-random-variance-percent) half-max-variance))))]
    (map #(multiplied (* radius (random-variance-factor)) (radians-to-xy %)) radians-of-edges)))

(defn warp-left [xy]
  (translated xy (- field-width) 0))

(defn warp-up [xy]
  (translated xy 0 (- field-height)))

(defn warp-right [xy]
  (translated xy field-width 0))

(defn warp-down [xy]
  (translated xy 0 field-height))

(defn warped-xy [{:keys [x y] :as position}]
  (let [tmp-x (if (< x 0) (+ x field-width) x)
        tmp-y (if (< y 0) (+ y field-height) y)
        new-x (if (> tmp-x field-width) (- tmp-x field-width) tmp-x)
        new-y (if (> tmp-y field-height) (- tmp-y field-height) tmp-y)]
    (if (and (= new-x x) (= new-y y)) position (new xy new-x new-y))))

(defn transformed-xy [{:keys [x y]} affine-transform]
  (TransformUtils/transform affine-transform x y))

(defn translation-of [affine-transform]
  (let [pt (TransformUtils/transform affine-transform 0 0)]
    {:x (.x pt) :y (.y pt)}))

(defn xy-to-area [xys]
  (let [polygon (new java.awt.Polygon)]
    (doall (for [point xys] [(.addPoint polygon (:x point) (:y point))]))
    (new Area polygon)))

(defn intersect? [shape shape2]
  (let [clone (.clone shape)
        _ (.intersect clone shape2)]
    (not (.isEmpty clone))))

(defn with-max-length [max-length xy]
  (let [length-of-xy (length-of xy)]
    (if
      (< max-length length-of-xy)
      (with-length max-length xy)
      xy)))

;generation helper functions 

(defn initial-speed-by-radius [radius]
  (/ radius))

(defn initial-spin-by-radius [radius]
  (/ radius))

(defn gen-random-direction [] (* 2 (num (random-double)) Math/PI))

(defn gen-random-normalized-vector [] (radians-to-xy (gen-random-direction)))

(defn initally-shift-position [position]
  (translated position (* 0.5 field-width) (* 0.5 field-height)))

(defn gen-asteroid-starting-position []
  (initally-shift-position
    (multiplied
      (/ (min field-height field-width) (+ 1.0 (random-double)) 2)
      (gen-random-normalized-vector))))

(defn gen-asteroid-movement [radius] (multiplied (initial-speed-by-radius radius) (gen-random-normalized-vector)))

(defn gen-random-spin [radius] (* (initial-spin-by-radius radius) (- (random-double) 0.5)))

;area definitions
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

(defn gen-bullet-area []
  (let [polygon (new java.awt.Polygon)]
    (.addPoint polygon 0 0)
    (.addPoint polygon 1 0)
    (.addPoint polygon 1 1)
    (.addPoint polygon 0 1)
    (new Area polygon)))

;advance-functions
(defn updated-transform
  [{{:keys [x y]} :position rotation :rotation}]
  (let [new-transform (new AffineTransform)
        _ (.rotate new-transform rotation x y)
        _ (.translate new-transform x y)
        ]
    new-transform))

(defn updated-position
  [{old-position :position {mov-x :x mov-y :y} :movement}]
  (translated old-position mov-x mov-y))

(defn updated-shape
  ([{:keys [transform shape]}]
    (updated-shape transform shape))
  ([transform shape]
    (.createTransformedArea shape transform)))

(defn updated-rotation [{:keys [rotation spin]}]
  (+ rotation spin))

(defn collision-result [{:keys [all-warped-shapes]} test-against]
  (for [my-shape all-warped-shapes
        other-entity test-against
        other-shape (:all-warped-shapes other-entity) :when (intersect? my-shape other-shape)]
    [my-shape other-shape]))

(defn collision? [entity test-against]
  (not-empty (collision-result entity test-against)))

(defn chain-assoc [org key function & keys-and-functions]
  (let [updated (assoc org key (function org))]
    (if
      (empty? keys-and-functions)
      updated
      (apply chain-assoc
        (concat [updated (first keys-and-functions) (second keys-and-functions)] (drop 2 keys-and-functions))))))

(defn touches-left-border? [shape]
  (.intersects shape left-border-line))

(defn touches-right-border? [shape]
  (.intersects shape right-border-line))

(defn touches-top-border? [shape]
  (.intersects shape top-border-line))

(defn touches-bottom-border? [shape]
  (.intersects shape bottom-border-line))

(defn warped-right [shape]
  (.createTransformedArea shape right-warp-transform))

(defn warped-left [shape]
  (.createTransformedArea shape left-warp-transform))

(defn warped-up [shape]
  (.createTransformedArea shape up-warp-transform))

(defn warped-down [shape]
  (.createTransformedArea shape down-warp-transform))

(defn all-warped-shapes [{transformed-shape :transformed-shape}]
  (filter #(not (nil? %))
    [(if (touches-left-border? transformed-shape) (warped-right transformed-shape) nil)
     (if (touches-right-border? transformed-shape) (warped-left transformed-shape) nil)
     (if (touches-top-border? transformed-shape) (warped-down transformed-shape) nil)
     (if (touches-bottom-border? transformed-shape) (warped-up transformed-shape) nil)
     transformed-shape]))

(defn with-updated-transforms [entity]
  (chain-assoc entity
    :transform updated-transform
    :transformed-shape updated-shape
    :all-warped-shapes all-warped-shapes))

(defn updated-asteroid [{:keys [asteroids bullets ship]} self]
  (let [collision-detected? (collision? self bullets)]
    (if collision-detected?
      []
      [(let [after-first-update
             (assoc self
               :position (warped-xy (updated-position self))
               :rotation (updated-rotation self))]
         (with-updated-transforms after-first-update))])))

(defn updated-spin [{:keys [spin]} left? right?]
  (let [left-applied (if left? (- spin spin-acc) spin)
        right-applied (if right? (+ left-applied spin-acc) left-applied)
        abs-spin (Math/abs (double right-applied))
        max-applied (if (> abs-spin max-spin-speed) (* max-spin-speed (Math/signum right-applied)) right-applied)]
    max-applied))

(defn slowed-down-movement [{movement :movement}]
  (multiplied 0.9 movement))

(defn slowed-down-spin [{spin :spin}]
  (* 0.85 spin))


(defn updated-movement [{:keys [movement rotation]} thrust? break?]
  (:pre [movement rotation thrust? break?])
  (let [direction-xy (radians-to-xy rotation)
        thrust-applied
        (if thrust?
          (with-max-length
            max-forward-speed
            (translated movement (with-length forward-acc direction-xy)))
          movement)
        break-applied
        (if break?
          (with-max-length
            max-backward-speed
            (translated thrust-applied (neg (with-length backward-acc direction-xy))))
          thrust-applied)]
    break-applied))

(defn updated-ship [{:keys [asteroids]} self user-input]
  (if
    (nil? self)
    nil
    (let [{:keys [left? right? thrust? break? attack?]} (destructured user-input)
          collision-detected? (collision? self asteroids)]
      (if collision-detected?
        nil
        (let [after-first-update
              (chain-assoc self
                :spin #(updated-spin % left? right?)
                :movement #(updated-movement % thrust? break?)
                :position #(warped-xy (updated-position %))
                :rotation updated-rotation
                :spin slowed-down-spin
                :movement slowed-down-movement)]
          (with-updated-transforms after-first-update))))))

(defn split-up-asteroids [asteroids]
  [])

(defn updated-bullet [{:keys [asteroids]} self]
  (let [touched-asteroids (collision-result self asteroids)]
    (if (empty? touched-asteroids)
      [(let [after-first-update
             (assoc self
               :position (warped-xy (updated-position self)))]
         (with-updated-transforms after-first-update))]
      (split-up-asteroids touched-asteroids))))

;global update function
(defn updated-world [{:keys [ship bullets asteroids] :as world} user-input]
  {:asteroids (flatten (map #(updated-asteroid world %) asteroids))
   :bullets (flatten (map #(updated-bullet world %) bullets))
   :ship (updated-ship world ship user-input)
   })

;object generation functions
(defn gen-asteroid [size]
  (let [edges (random-int-in-range 10 (max 11 size))
        area (xy-to-area (gen-circle-outline size edges 0.45))
        {:keys [x y]} (gen-asteroid-starting-position)
        movement (gen-asteroid-movement size)
        spin (gen-random-spin size)
        initial-transform (AffineTransform/getTranslateInstance x y)]
    {:movement movement
     :spin spin
     :rotation 0.0
     :position {:x x :y y}
     :transform initial-transform
     :shape area
     :transformed-shape (updated-shape initial-transform area)}))

(defn gen-ship []
  (let [area (gen-ship-area)
        initial-transform (new AffineTransform)]
    {:movement (new xy 0 0)
     :spin 0.0
     :rotation 0.0
     :position (initally-shift-position {:x 0.0 :y 0.0})
     :transform initial-transform
     :shape area
     :transformed-shape (updated-shape initial-transform area)}))

(defn gen-bullet [ship]
  (let [{rotation :rotation transform :transform} ship
        {:keys [x y]} (:position ship)
        area (gen-bullet)
        initial-transform (AffineTransform/getTranslateInstance x y)
        direction (radians-to-xy rotation)]
    {:movement (with-length 5 direction)
     :spin 0.0
     :rotation 0.0
     :position {:x x :y y}
     :transform initial-transform
     :shape area
     :transformed-shape (updated-shape initial-transform area)}))

(defn gen-world [asteroidcount]
  (let [gen-demo-asteroid #(gen-asteroid (random-int-in-range 5 35))
        asteroids (repeatedly asteroidcount gen-demo-asteroid)
        player-ship (gen-ship)]
    {:ship player-ship
     :asteroids asteroids
     :bullets []}))

;rendering
(defn render [{:keys [asteroids bullets ship]} render-target]
  (let [graphics (.getGraphics render-target)]
    (doseq [element (concat [ship] bullets asteroids) :when (not (nil? element))]
      (doseq [shape (:all-warped-shapes element)]
        (.draw graphics shape)))))

;main
(let [user-input-atom (atom [])
      world-atom (atom (gen-world 25))
      uiaccess (MainFrame/createFrame field-width field-height)
      callback
      (reify AdvanceCallback
        (onTick [this render-target]
          (swap! user-input-atom (fn [discard] (.getMostRecentUserInput uiaccess)))
          (swap! world-atom updated-world @user-input-atom)
          (render @world-atom render-target)))]
  (.initAdvanceCallback uiaccess callback))

