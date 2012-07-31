(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic
        [webdriver-logic.state :only [*driver* *html-tags* *html-attributes*]]
        [webdriver-logic.util :only [fresh? ground?]])
  (:require [clojure.test :as t]
            [clj-webdriver.core :as wd]
            [webdriver-logic.state :as st])
  (:import [org.openqa.selenium InvalidElementStateException]
           [org.openqa.selenium.remote ErrorHandler$UnknownServerException]))

(defmacro s
  "Deterministic test. Deterministic predicates are predicates that must succeed exactly once and, for well behaved predicates, leave no choicepoints.

   This form is not concerned with the actual value returned, just that the run was successful and returned only one value."
  [run-body]
  `(let [goal-values# ~run-body]
     (t/is (= (count goal-values#) 1))))

(defmacro s+
  "Assert that a run returns more than one value (non-deterministic).

   This form is not concerned with the actual values returned, just that the run was successful and returned more than one value."
  [run-body]
  `(let [goal-values# ~run-body]
     (t/is (> (count goal-values#) 1))))

(defmacro u
  "Assert that a run fails."
  [run-body]
  `(let [goal-values# ~run-body]
     (t/is (not (seq goal-values#)))))

(defmacro s-as
  "Assert that the run is successful and returns a sequence of values equivalent to `coll`. If only a single value is expected, `coll` may be this standalone value."
  [coll run-body]
  `(let [goal-values# ~run-body
         a-coll# (if (coll? ~coll)
                   ~coll
                   '(~coll))]
     (t/is (= a-coll# goal-values#))))

(defmacro s-includes
  "Assert that the run is successful and that the items in `coll` are included in the return value. The items in `coll` need not be exhaustive; the assertion only fails if one of the items in `coll` is not returned from the run."
  [coll run-body]
  `(let [goal-values# ~run-body
         a-coll# (if (coll? ~coll)
                   ~coll
                   '(~coll))]
     (t/is (not (some nil?
                      (map #(some #{%} goal-values#) a-coll#))))))

;; Redefined here for API convenience
(defn set-driver!
  ([browser-spec] (st/set-driver! browser-spec))
  ([browser-spec url] (st/set-driver! browser-spec url)))

;; Kudos to http://tsdh.wordpress.com/2012/01/06/using-clojures-core-logic-with-custom-data-structures/

(def
  ^{:dynamic true
    :doc "Limit any calls to `clj-webdriver.core/find-elements` to this domain. Expected to be a Clojure form that can act as that function's second argument."}
  *search-domain* {:xpath "//*"})

(def
  ^{:dynamic true
    :doc "Limit any calls to `clj-webdriver.core/find-elements` **for which the first argument is an Element record** to this domain. This signature searches for elements that are children of this first parameter, hence the name `child-search-domain`. This value should be a Clojure form that can act as the function's second argument."}
  *child-search-domain* {:xpath ".//*"})

(defn all-elements
  "Shortcut for using WebDriver to get all elements"
  []
  (wd/find-elements *driver* *search-domain*))

(defn all-child-elements
  "Shortcut for using WebDriver to get all elements beneath an element"
  [parent-elem]
  (wd/find-elements parent-elem *child-search-domain*))

;; ### Relations ###
;;
;; See the webdriver-logic.test.benchmarks namespaces for performance details

(defn attributeo
  "A relation where `elem` has value `value` for its `attr` attribute"
  [elem attr value]
  (fn [a]
    (let [gelem (walk a elem)
          gattr (walk a attr)
          gvalue (walk a value)]
      (cond
        (and (ground? gelem)
             (ground? gattr)) (unify a
                                     [elem attr value]
                                     [gelem gattr (try
                                                    (wd/attribute gelem gattr)
                                                    (catch InvalidElementStateException _ nil)
                                                    (catch ErrorHandler$UnknownServerException _ nil))])
        (ground? gelem) (to-stream
                         (for [attribute *html-attributes*]
                           (unify a
                                  [elem attr value]
                                  [gelem attribute (try
                                                     (wd/attribute gelem attribute)
                                                     (catch InvalidElementStateException e nil)
                                                     (catch ErrorHandler$UnknownServerException _ nil))])))
        (ground? gattr) (to-stream
                         (for [element (all-elements)]
                           (unify a
                                  [elem attr value]
                                  [element gattr (try
                                                   (wd/attribute element gattr)
                                                   (catch InvalidElementStateException e nil)
                                                   (catch ErrorHandler$UnknownServerException _ nil))])))
        :default (to-stream
                  (for [element (all-elements)
                        attribute *html-attributes*]
                    (unify a
                           [elem attr value]
                           [element attribute (try
                                                (wd/attribute element attribute)
                                                (catch InvalidElementStateException e nil)
                                                (catch ErrorHandler$UnknownServerException _ nil))])))))))

;; TODO: You can't put q everywhere, `parent-elem` is assumed grounded
(defn childo
  "A relation where `child-elem` is a child element of the `parent-elem` element on the current page."
  [child-elem parent-elem]
  (fn [a]
    (to-stream
     (map #(unify a
                  [child-elem parent-elem]
                  [% parent-elem])
          (all-child-elements parent-elem)))))

(defn displayedo
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (for [el (all-elements)]
           (if (wd/displayed? el)
             (unify a
                    elem
                    el)
             (fail a))))
        (if (wd/displayed? gelem)
          (unify a
                 elem
                 gelem)
          (fail a))))))

(defn enabledo
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (for [el (all-elements)]
           (if (wd/enabled? el)
             (unify a
                    elem
                    el)
             (fail a))))
        (if (wd/enabled? gelem)
          (unify a
                 elem
                 gelem)
          (fail a))))))

(defn existso
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (for [el (all-elements)]
           (if (wd/exists? el)
             (unify a
                    elem
                    el)
             (fail a))))
        (if (wd/exists? gelem)
          (unify a
                 elem
                 gelem)
          (fail a))))))

(defn intersecto [])

(defn presento
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (for [el (all-elements)]
           (if (wd/present? el)
             (unify a
                    elem
                    el)
             (fail a))))
        (if (wd/present? gelem)
          (unify a
                 elem
                 gelem)
          (fail a))))))

(defn selected
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (for [el (all-elements)]
           (if (wd/selected? el)
             (unify a
                    elem
                    el)
             (fail a))))
        (if (wd/selected? gelem)
          (unify a
                 elem
                 gelem)
          (fail a))))))

(defn sizeo
  [elem size]
  (fn [a]
    (let [gelem (walk a elem)
          gsize (walk a size)]
      (cond
        (ground? gelem) (unify a
                               [elem size]
                               [gelem (wd/size gelem)])
        (ground? gsize)  (to-stream
                          (for [el (all-elements)]
                            (unify a
                                   [elem size]
                                   [el (wd/size el)])))
        :default        (to-stream
                         (for [el (all-elements)]
                           (unify a
                                  [elem size]
                                  [el (wd/size el)])))))))

(defn tago
  "This `elem` has this `tag` name"
  [elem tag]
  (fn [a]
    (let [gelem (walk a elem)
          gtag (walk a tag)]
      (cond
        (ground? gelem) (unify a
                               [elem tag]
                               [gelem (wd/tag gelem)])
        (ground? gtag)  (to-stream
                         (for [el (all-elements)]
                           (unify a
                                  [elem tag]
                                  [el (wd/tag el)])))
        :default        (to-stream
                         (for [el (all-elements)]
                           (unify a
                                  [elem tag]
                                  [el (wd/tag el)])))))))

(defn texto
  [elem text]
  (fn [a]
    (let [gelem (walk a elem)
          gtext (walk a text)]
      (cond
        (ground? gelem) (unify a
                               [elem text]
                               [gelem (wd/text gelem)])
        (ground? gtext)  (to-stream
                         (for [el (all-elements)]
                           (unify a
                                  [elem text]
                                  [el (wd/text el)])))
        :default        (to-stream
                         (for [el (all-elements)
                               a-text (map wd/text (all-elements))]
                           (unify a
                                  [elem text]
                                  [el (wd/text el)])))))))

(defn valueo
  "Shortcut for attributeo with `value` attribute"
  [elem value]
  (attributeo elem "value" value))

(defn visibleo
  "Visible elements"
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (for [el (all-elements)]
           (if (wd/visible? el)
             a
             (fail a))))
        (if (wd/visible? gelem)
          a
          (fail a))))))

(comment

  (def b (wd/start {:browser :firefox
                    :cache-spec {:strategy :basic
                                 :args [{}]
                                 :include [ {:xpath "//a"} ]}
                    }
                   ;;"https://github.com"
                   "http://localhost:5744"))
  (set-driver! b)

  (do
    (wd/click (wd/find-element *driver* {:css "a[href*='login']"}))
    (wd/input-text (wd/find-element *driver* {:css "input#login_field"}) "semperos"))

  )