(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clj-webdriver.core :as wd]))

(defn attributeo
  "A relation where `elem` has value `value` for its `attr` attribute"
  [driver elem attr value]
  (fn [a]
    (to-stream
     (->> (for [el (wd/find-elements driver {:xpath "//*"})
                :let [attribute (keyword attr)]]
            (unify a
                   [elem attr value]
                   [el attribute (wd/attribute el attribute)]))
          (remove not)))))