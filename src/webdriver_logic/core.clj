(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic
        [clj-webdriver.driver :only [driver?]]
        [webdriver-logic.state :only [*html-tags* *html-attributes*]]
        [webdriver-logic.util :only [fresh? ground? careful-attribute]]
        [clojure.pprint :only [pprint]])
  (:require [clojure.test :as test]
            [clj-webdriver.core :as wd]
            [webdriver-logic.state :as st]))

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
  "Set the `clj-webdriver.driver.Driver` record to be used with this API."
  ([browser-spec] (set-driver* browser-spec))
  ([browser-spec url] (wd/to (set-driver* browser-spec) url)))

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
  "Shortcut for using WebDriver to get all elements beneath an element. Deletes any Element records that have a nil `:webelement` entry."
  [parent-elem]
  (remove #(nil? (:webelement %)) (wd/find-elements parent-elem *child-search-domain*)))

;; ### Relations ###
;;
;; See the webdriver-logic.test.benchmarks namespaces for performance details

(defn attributeo
  "A goal that succeeds if the attribute `attr` has a value `value` for the given `elem` on the current page"
  [elem attr value]
  (fn [a]
    (let [gelem (walk a elem)
          gattr (walk a attr)
          gvalue (walk a value)]
      (cond
        (and (ground? gelem)
             (ground? gattr)) (unify a
                                     [elem attr value]
                                     [gelem gattr (careful-attribute gelem gattr)])
        (ground? gelem) (to-stream
                         (for [attribute *html-attributes*]
                           (unify a
                                  [elem attr value]
                                  [gelem attribute (careful-attribute gelem attribute)])))
        (ground? gattr) (to-stream
                         (for [element (all-elements)]
                           (unify a
                                  [elem attr value]
                                  [element gattr (careful-attribute element gattr)])))
        :default (to-stream
                  (for [element (all-elements)
                        attribute *html-attributes*]
                    (unify a
                           [elem attr value]
                           [element attribute (careful-attribute element attribute)])))))))

(defn childo
  "A goal that succeeds if the `child-elem` is a child of the `parent-elem` on the current page"
  [child-elem parent-elem]
  (fn [a]
    (let [gchild (walk a child-elem)
          gparent (walk a parent-elem)]
      (cond
        (and (ground? gparent)
             (ground? gchild)) (if (some #{gchild} (all-child-elements gparent))
                                 (unify a
                                        [child-elem parent-elem]
                                        [gchild gparent])
                                 (fail a))
             (ground? gparent) (to-stream
                                (map #(unify a
                                             [child-elem parent-elem]
                                             [% gparent])
                                     (all-child-elements gparent)))
             (ground? gchild) (to-stream
                               (flatten
                                (for [el-parent (all-elements)]
                                  (map #(unify a
                                               [child-elem parent-elem]
                                               [% el-parent])
                                       (all-child-elements el-parent)))))
             :default        (to-stream
                              (flatten
                               (for [el-parent (all-elements)]
                                 (map #(unify a
                                              [child-elem parent-elem]
                                              [% el-parent])
                                      (all-child-elements el-parent)))))))))

(defn displayedo
  "A goal that succeeds if the given `elem` is displayed (visible) on the current page"
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
  "A goal that succeeds if the given `elem` is enabled on the current page"
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
  "A goal that succeeds if the given `elem` exists on the current page (regardless of other attributes)"
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
  "A goal that succeeds if the given `elem` both exists and is visible on the current page"
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

(defn selectedo
  "A goal that succeeds if the given `elem` is selected on the current page"
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
  "A goal that succeeds if `size` unifies with the size of the given `elem` on the current page"
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
  "A goal that succeeds if `tag` unifies with the tag name of the given `elem` on the current page"
  [elem tag]
  (fn [a]
    (let [gelem (walk a elem)]
      (cond
        (ground? gelem) (unify a tag (wd/tag gelem))
        :default        (to-stream
                         (map #(unify a [elem tag] [% (wd/tag %)])
                              (all-elements)))))))

(defn texto
  "A goal that succeeds if `text` unifies with the textual content of the given `elem` on the current page"
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

(defn visibleo
  "A goal that succeeds if the given `elem` is visible on the current page"
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

  ;; This webdriver-logic.core ns should use clj-webdriver.core (as it does)
  ;; but using Taxi is preferred for higher-level use
  (require '[clj-webdriver.taxi :as t])
  ;; Defining a var in this ns for convenience
  (def b (wd/start {:browser :firefox
                    :cache-spec {:strategy :basic
                                 :args [{}]
                                 :include [ {:xpath "//a"} ]}
                    }
                   "https://github.com"
                   ;; "http://localhost:5744"
                   ))
  ;; For the Taxi API
  (t/set-driver! b)
  ;; For webdriver-logic, to make code more concise
  (set-driver! b)

  (do
    (wd/click (wd/find-element *driver* {:css "a[href*='login']"}))
    (wd/input-text (wd/find-element *driver* {:css "input#login_field"}) "semperos"))

  )