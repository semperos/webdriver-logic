(ns ^{:doc "Utilties atop third-party dependendies."}
  webdriver-logic.util
  (:use [clojure.core.logic :only [lvar?]])
  (:require [clj-webdriver.core :as wd])
  (:import [org.openqa.selenium InvalidElementStateException]
           [org.openqa.selenium.remote ErrorHandler$UnknownServerException]))

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

(defmacro careful-attribute
  "Wrap calls to wd/attribute in try and catch standard exceptions"
  [elem attr]
  `(try
     (wd/attribute ~elem ~attr)
     (catch InvalidElementStateException _# nil)
     (catch ErrorHandler$UnknownServerException _# nil)))