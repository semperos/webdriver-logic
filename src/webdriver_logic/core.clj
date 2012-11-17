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
                         (map #(unify a
                                      [elem attr value]
                                      [gelem % (careful-attribute gelem %)])
                              *html-attributes*))
        (ground? gattr) (to-stream
                         (map #(unify a
                                      [elem attr value]
                                      [% gattr (careful-attribute % gattr)])
                              (all-elements)))
        :default (to-stream
                  ;; Use `for` here because we need all combinations
                  ;; of elements with legal attributes
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
                                 a
                                 (fail a))
             (ground? gparent) (to-stream
                                (map #(unify a
                                             [child-elem parent-elem]
                                             [% gparent])
                                     (all-child-elements gparent)))
             :default        (to-stream
                              (flatten
                               (for [el-parent (all-elements)]
                                 (map #(unify a
                                              [child-elem parent-elem]
                                              [% el-parent])
                                      (all-child-elements el-parent)))))))))

(defn current-urlo
  "A goal that succeeds if the given `current-url` unifies with the browser current-url"
  [current-url]
  (fn [a]
    (let [gcurrent-url (walk a current-url)
          browser-current-url (wd/current-url *driver*)]
      (if (fresh? gcurrent-url)
        (unify a current-url browser-current-url)
        (if (= gcurrent-url browser-current-url)
          a
          (fail a))))))

(defn displayedo
  "A goal that succeeds if the given `elem` is displayed (visible) on the current page"
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (map #(unify a elem %)
              (filter #(wd/displayed? %)
                      (all-elements))))
        (if (wd/displayed? gelem)
          a
          (fail a))))))

(defn enabledo
  "A goal that succeeds if the given `elem` is enabled on the current page"
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (map #(unify a elem %)
              (filter #(wd/enabled? %)
                      (all-elements))))
        (if (wd/enabled? gelem)
          a
          (fail a))))))

(defn existso
  "A goal that succeeds if the given `elem` exists on the current page (regardless of other attributes)"
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (map #(unify a elem %)
              (filter #(wd/exists? %)
                      (all-elements))))
        (if (wd/exists? gelem)
          a
          (fail a))))))

;; (defn intersecto [])

(defn locationo
  "A goal that succeeds if `location` unifies with the location of the given `elem` on the current page"
  [elem location]
  (fn [a]
    (let [gelem (walk a elem)
          glocation (walk a location)]
      (cond
        (ground? gelem) (unify a
                               [elem location]
                               [gelem (wd/location gelem)])
        :default        (to-stream
                         (map #(unify a
                                      [elem location]
                                      [% (wd/location %)])
                              (all-elements)))))))

(defn presento
  "A goal that succeeds if the given `elem` both exists and is visible on the current page"
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (map #(unify a elem %)
              (filter #(wd/present? %)
                      (all-elements))))
        (if (wd/present? gelem)
          a
          (fail a))))))

(defn selectedo
  "A goal that succeeds if the given `elem` is selected on the current page"
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (map #(unify a elem %)
              (filter #(wd/selected? %)
                      (all-elements))))
        (if (wd/selected? gelem)
          a
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
        :default        (to-stream
                         (map #(unify a
                                      [elem size]
                                      [% (wd/size %)])
                              (all-elements)))))))

(defn tago
  "A goal that succeeds if `tag` unifies with the tag name of the given `elem` on the current page"
  [elem tag]
  (fn [a]
    (let [gelem (walk a elem)]
      (cond
        (ground? gelem) (unify a tag (wd/tag gelem))
        :default        (to-stream
                         (map #(unify a
                                      [elem tag]
                                      [% (wd/tag %)])
                              (all-elements)))))))

(defn texto
  "A goal that succeeds if `text` unifies with the textual content of the given `elem` on the current page"
  [elem text]
  (fn [a]
    (let [gelem (walk a elem)]
      (cond
        (ground? gelem) (unify a text (wd/text gelem))
        :default        (to-stream
                         (map #(unify a
                                      [elem text]
                                      [% (wd/text %)])
                              (all-elements)))))))

(defn titleo
  "A goal that succeeds if the given `title` unifies with the browser title"
  [title]
  (fn [a]
    (let [gtitle (walk a title)
          browser-title (wd/title *driver*)]
      (if (fresh? gtitle)
        (unify a title browser-title)
        (if (= gtitle browser-title)
          a
          (fail a))))))

(defn visibleo
  "A goal that succeeds if the given `elem` is visible on the current page"
  [elem]
  (fn [a]
    (let [gelem (walk a elem)]
      (if (fresh? gelem)
        (to-stream
         (map #(unify a elem %)
              (filter #(wd/visible? %)
                      (all-elements))))
        (if (wd/visible? gelem)
          a
          (fail a))))))

(comment
  (require '[clj-webdriver.taxi :as t])
  (def b (wd/start {:browser :chrome}
                   ;; "https://github.com"
                   "http://localhost:5744"
                   ))
  ;; For the Taxi API
  (t/set-driver! b)
  ;; For webdriver-logic, to make code more concise
  (set-driver! b)
)