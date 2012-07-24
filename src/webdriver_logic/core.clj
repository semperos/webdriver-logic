(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clj-webdriver.core :as wd]))

(defn classo
  "A relation where `value` is the value of the class attribute for `elem`"
  [driver elem value]
  (fn [a]
    (to-stream
     (->> (for [el (wd/find-elements driver {:xpath "//*"})
                :let [attr :class]]
            (unify a
                   [elem value]
                   [el (wd/attribute el attr)]))
          (remove not)))))