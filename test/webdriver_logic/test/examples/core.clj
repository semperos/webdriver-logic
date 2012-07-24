(ns webdriver-logic.examples.core
  (:refer-clojure :exclude [==])
  (:use webdriver-logic.core :reload
        clojure.core.logic)
  (:require [clj-webdriver.core :as wd]))

(comment
  (def b (wd/start {:browser :chrome} "https://github.com")))