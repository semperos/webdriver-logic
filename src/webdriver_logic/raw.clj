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
  *child-search-domain* [:* :*])

(defn source-tree
  "Parse in source code for the current page of the given driver using Enlive"
  ([] (source-tree webdriver-logic.state/*driver*))
  ([driver]
     (if (wd-cache/in-cache? driver :page-source)
       (first (wd-cache/retrieve driver :page-source))
       (let [src (h/html-resource (java.io.StringReader. (wd/page-source driver)))]
         (wd-cache/insert driver :page-source src)
         src))))

(defn all-elements
  "Shortcut for using Enlive to read source and return all elements"
  []
  (let [tree (source-tree *driver*)]
    (h/select tree *search-domain*)))

(defn all-child-elements
  "Shortcut for using Enlive to get all elements beneath an element"
  [parent-elem]
  (filter map? (h/select parent-elem *child-search-domain*)))

(defn first-element
  "First item in the contents of an Enlive node that represents another element/node."
  [node-contents]
  (first (filter map? node-contents)))

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
  "NOTE: This causes an error with current released versions of Enlive, due to how core.logic unifies on maps (specifically the StructMap `element` used by Enlive).

   A relation where `child-elem` is a child element of the `parent-elem` element on the current page."
  [child-elem parent-elem]
  (fn [a]
    (println (count (all-child-elements (first (all-elements)))))
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

(defn existso [elem]
  "NOTE: This causes an error with current released versions of Enlive, due to how core.logic unifies on maps (specifically the StructMap `element` used by Enlive)."
  (membero elem (all-elements)))

(defn selectedo
  [elem]
  (attributeo elem :selected "selected"))

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

;; Could ostensibly offer something here, but would require manipulating Enlive's element structs
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