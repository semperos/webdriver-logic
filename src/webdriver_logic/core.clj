(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clj-webdriver.core :as wd]))

;; Driver-based relations
(defn current-urlo
  [driver url]
  (== (wd/current-url driver) url))

(defn titleo
  [driver title]
  (== (wd/title driver) title))

(defn page-sourceo
  [driver page-source]
  (== (wd/page-source driver) page-source))

;; Element-based relations

(defn attributeo
  [element attr value]
  (== (wd/attribute element attr) value))

(defn valueo
  [element value]
  (attributeo element :value value))

(defn clearo
  [element]
  (attributeo element :value ""))

(defn selectedo
  [element]
  (== (wd/selected? element) true))

(defn visibleo
  [element]
  (== (wd/visible? element) true))

(defn tag-nameo
  [element tag-name]
  (== (wd/tag-name element) tag-name))

(defn enabledo
  [element]
  (== (wd/enabled? element) true))

;; existso will be implemented post clj-webdriver 0.5.x

(defn texto
  [element text]
  (== (wd/text element) text))

(defn htmlo
  [element html]
  (== (wd/html element) html))

(defn xpatho
  [element xpath]
  (== (wd/xpath element) xpath))

(defn locationo
  [element loc-map]
  (== (wd/location element) loc-map))

(defn location-once-visibleo
  [element loc-map]
  (== (wd/location-once-visible element) loc-map))

