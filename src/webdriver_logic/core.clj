(ns webdriver-logic.core
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic
        [webdriver-logic.state :only [set-driver! *driver*
                                      *html-tags* *html-attributes*]]
        [webdriver-logic.util :only [fresh? ground?]])
  (:require [clj-webdriver.core :as wd]))

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
                                     [gelem gattr (wd/attribute gelem gattr)])
        (ground? gelem) (to-stream
                         (for [attribute *html-attributes*]
                           (unify a
                                  [elem attr value]
                                  [gelem attribute (wd/attribute gelem attribute)])))
        (ground? gattr) (to-stream
                         (for [element (all-elements)]
                           (unify a
                                  [elem attr value]
                                  [element gattr (wd/attribute element gattr)])))
        :default (to-stream
                  (for [element (all-elements)
                        attribute *html-attributes*]
                    (unify a
                           [elem attr value]
                           [element attribute (wd/attribute element attribute)])))))))

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

  (set-driver! {:browser :firefox
                    :cache-spec {:strategy :basic
                                 :args [{}]
                                 :include [ {:xpath "//a"} ]}
                    }
               "https://github.com")

  (do
    (wd/click (wd/find-element *driver* {:css "a[href*='login']"}))
    (wd/input-text (wd/find-element *driver* {:css "input#login_field"}) "semperos"))

  )