(ns ^{:doc "Raw equivalents to some of the core API. 'Raw' signifies testing against the raw source code of a web page, instead of interacting with it via the Selenium-WebDriver API."}
  webdriver-logic.raw
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic
        [webdriver-logic.state :only [set-driver! *driver*
                                      *html-tags* *html-attributes*]]
        [webdriver-logic.util :only [fresh? ground?]])
  (:require [clj-webdriver.core :as wd]
            [clj-webdriver.cache :as wd-cache]
            [net.cgrand.enlive-html :as h]))

(def
  ^{:dynamic true
    :doc "Limit Enlive's querying to the following."}
  *search-domain* [:*])

(def
  ^{:dynamic true
    :doc "Limit Enlive's querying for *children* to the following."}
  *child-search-domain* [:*])

(defn get-source
  "Parse in source code for the current page of the given driver using Enlive"
  [driver]
  (if (wd-cache/in-cache? driver :page-source)
    (first (wd-cache/retrieve driver :page-source))
    (let [src (h/html-resource (java.io.StringReader. (wd/page-source driver)))]
      (wd-cache/insert driver :page-source src)
      src)))

(defn all-elements
  "Shortcut for using Enlive to read source and return all elements"
  []
  (let [tree (get-source *driver*)]
    (h/select tree *search-domain*)))

(defn all-child-elements
  "Shortcut for using Enlive to get all elements beneath an element"
  [parent-elem]
  (let [tree (get-source *driver*)]
    (h/select tree (concat parent-elem *child-search-domain*))))

;; ### Relations ###
;;
;; See the webdriver-logic.test.benchmarks namespaces for performance details

(defn attributeo
  "Same as `attributeo`, but use the source of the page with Enlive"
  [elem attr value]
  (fn [a]
    (let [gelem (walk a elem)
          gattr (walk a attr)
          gvalue (walk a value)]
      (cond
        (and (ground? gelem)
             (ground? gattr)) (unify a
                                     [elem attr value]
                                     [gelem gattr (get-in gelem [:attrs gattr])])
        (ground? gelem) (to-stream
                         (for [attribute *html-attributes*]
                           (unify a
                                  [elem attr value]
                                  [gelem attribute (get-in gelem [:attrs attribute])])))
        (ground? gattr) (to-stream
                         (for [element (all-elements)]
                           (unify a
                                  [elem attr value]
                                  [element gattr (get-in element [:attrs gattr])])))
        :default (to-stream
                  (for [element (all-elements)
                        attribute *html-attributes*]
                    (unify a
                           [elem attr value]
                           [element attribute (get-in element [:attrs attribute])])))))))

(defn childo
  "Same as `childo`, but use the source of the page with Enlive"
  [child-elem parent-elem]
  (fn [a]
    (to-stream
     (map #(unify a
                  [child-elem parent-elem]
                  [% parent-elem])
          (all-child-elements parent-elem)))))

(defn existso [elem])
(defn selectedo [elem])

(defn tago
  [elem tag]
  (fn [a]
    (let [gelem (walk a elem)
          gtag (walk a tag)]
      (cond
        (ground? gelem) (unify a
                               [elem tag]
                               [gelem (:tag gelem)])
        (ground? gtag)  (to-stream
                         (for [el (all-elements)]
                           (unify a
                                  [elem tag]
                                  [el (:tag el)])))
        :default        (to-stream
                         (for [el (all-elements)]
                           (unify a
                                  [elem tag]
                                  [el (:tag el)])))))))

;; I feel as though this is feasible, needs consideration how to manipulate
;; the tree that Enlive produces.
;; (defn texto)

(defn valueo
  "Shortcut for attributeo with `value` attribute"
  [elem value]
  (attributeo elem :value value))


(comment

  (use '[clj-webdriver.core :as wd])
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