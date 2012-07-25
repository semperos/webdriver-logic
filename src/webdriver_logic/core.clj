(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clj-webdriver.core :as wd]
            [clj-webdriver.element :as el]))

;; Kudos to http://tsdh.wordpress.com/2012/01/06/using-clojures-core-logic-with-custom-data-structures/

(def
  ^{:dynamic true
    :doc "Limit any calls to `clj-webdriver.core/find-elements` to this domain. Expected to be a Clojure form that can act as that function's second argument."}
  *search-domain* {:xpath "//*"})

(def
  ^{:dynamic true
    :doc "Limit any calls to `clj-webdriver.core/find-elements` **for which the first argument is an Element record** to this domain. This signature searches for elements that are children of this first parameter, hence the name `child-search-domain`. This value should be a Clojure form that can act as the function's second argument."}
  *child-search-domain* {:xpath ".//*"})

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

(defn attributeo
  "A relation where `elem` has value `value` for its `attr` attribute"
  [driver elem attr value]
  (fn [a]
    (to-stream
     (->> (for [el (wd/find-elements driver *search-domain*)
                :let [attribute (keyword attr)]]
            (unify a
                   [elem attr value]
                   [el attribute (wd/attribute el attribute)]))
          (remove not)))))

(defn childo
  "A relation where `child-elem` is a child element of the `parent-elem` element on the current page."
  [driver child-elem parent-elem]
  (fn [a]
    (to-stream
     (->> (map #(unify a
                       [child-elem parent-elem]
                       [% parent-elem])
               (wd/find-elements parent-elem *child-search-domain*))
          (remove not)))))

(comment

  (def b (wd/start {:browser :chrome
                    :cache-spec {:strategy :basic
                                 :args [{}]
                                 :include [ {:xpath "//a"} ]}}
                   "https://github.com"))

  )