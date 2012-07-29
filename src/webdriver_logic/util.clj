(ns ^{:doc "Utilties atop third-party dependendies."}
  webdriver-logic.util
  (:use [clojure.core.logic :only [lvar?]]))

(defn fresh?
  "Returns true, if `x' is fresh.
  `x' must have been `walk'ed before!"
  [x]
  (lvar? x))

(defn ground?
  "Returns true, if `x' is ground.
  `x' must have been `walk'ed before!"
  [x]
  (not (lvar? x)))