(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic
        [clj-webdriver.driver :only [driver?]])
  (:require [clj-webdriver.core :as wd]
            [clj-webdriver.element :as el]
            [clj-webdriver.cache :as wd-cache]
            [net.cgrand.enlive-html :as h]))

;; Kudos to http://tsdh.wordpress.com/2012/01/06/using-clojures-core-logic-with-custom-data-structures/

(def
  ^{:dynamic true
    :doc "The `Driver` instance to be used by the relations. Without this we'd be forced to pass in a 'grounded' var of the driver for every relation. Set this using set-driver!"}
  *driver*)

(defn- set-driver*
  "Given a `browser-spec`, instantiate a new Driver record and assign to `*driver*`."
  [browser-spec]
  (let [new-driver (if (driver? browser-spec)
                     browser-spec
                     (wd/new-driver browser-spec))]
       (alter-var-root (var *driver*)
                       (constantly new-driver)
                       (when (thread-bound? (var *driver*))
                         (set! *driver* new-driver)))))
(defn set-driver!
  ([browser-spec] (set-driver* browser-spec))
  ([browser-spec url] (wd/to (set-driver* browser-spec) url)))

(def
  ^{:dynamic true
    :doc "Limit any calls to `clj-webdriver.core/find-elements` to this domain. Expected to be a Clojure form that can act as that function's second argument."}
  *search-domain* {:xpath "//*"})

(def
  ^{:dynamic true
    :doc "Limit any calls to `clj-webdriver.core/find-elements` **for which the first argument is an Element record** to this domain. This signature searches for elements that are children of this first parameter, hence the name `child-search-domain`. This value should be a Clojure form that can act as the function's second argument."}
  *child-search-domain* {:xpath ".//*"})

(def
  ^{:dynamic true
    :doc "Limit Enlive's querying to the following."}
  *raw-search-domain* [:body :*])

(def
  ^{:dynamic true
    :doc "Limit Enlive's querying for *children* to the following."}
  *raw-child-search-domain* [:*])

(def
  ^{:dynamic true
    :doc "Set of 'legal' HTML attributes that should be part of the search space. Intentionally small by default, to make the search space smaller."}
  *html-attributes* #{:alt :class :for :href :id :name :src :value})

;; TODO: Consider lvaro nonlvaro
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

(defn get-source
  "Parse in source code for the current page of the given driver using Enlive"
  [driver]
  (if (wd-cache/in-cache? driver :page-source)
    (first (wd-cache/retrieve driver :page-source))
    (let [src (h/html-resource (java.io.StringReader. (wd/page-source driver)))]
      (wd-cache/insert driver :page-source src)
      src)))

;; ### Relations ###

;; Time: 5 *s*
(defn attributeo
  "A relation where `elem` has value `value` for its `attr` attribute"
  [elem attr value]
  (fn [a]
    (to-stream
     (->> (for [el (wd/find-elements *driver* *search-domain*)
                attribute *html-attributes*]
            (unify a
                   [elem attr value]
                   [el attribute (wd/attribute el attribute)]))
          (remove not)))))

;; Time: 35 ms
(defn raw-attributeo
  "Same as `attributeo`, but use the source of the page with Enlive"
  [elem attr value]
  (let [tree (get-source *driver*)]
    (fn [a]
      (to-stream
       (->> (for [el (h/select tree *raw-search-domain*)
                  attribute *html-attributes*]
              (unify a
                     [elem attr value]
                     [el attribute (get-in el [:attrs attribute])]))
            (remove not))))))

;; TODO: You can't put q everywhere, `parent-elem` is assumed grounded
;; Time: 13 ms
(defn childo
  "A relation where `child-elem` is a child element of the `parent-elem` element on the current page."
  [child-elem parent-elem]
  (fn [a]
    (to-stream
     (->> (map #(unify a
                       [child-elem parent-elem]
                       [% parent-elem])
               (wd/find-elements parent-elem *child-search-domain*))
          (remove not)))))

;; Time: 1.3 ms
(defn raw-childo
  "Same as `childo`, but use the source of the page with Enlive"
  [child-elem parent-elem]
  (let [tree (get-source *driver*)]
    (fn [a]
      (to-stream
       (->> (map #(unify a
                         [child-elem parent-elem]
                         [% parent-elem])
                 (h/select tree (concat parent-elem *raw-child-search-domain*)))
            (remove not))))))

(comment

  (def b (wd/start {:browser :firefox
                    :cache-spec {:strategy :basic
                                 :args [{}]
                                 :include [ {:xpath "//a"} ]}
                    }
                   "https://github.com"))

  )