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

;; See http://www.w3.org/2002/08/xhtml/xhtml1-transitional.xsd for tags and attributes

(def
  ^{:dynamic true
    :doc "List of legal HTML tag names that should be part of the search space."}
  *html-tags* #{"a" "abbr" "acronym" "address" "applet" "area" "b" "base" "basefont"
                "bdo" "big" "blockquote" "body" "br" "button" "caption" "center"
                "cite" "code" "col" "colgroup" "dd" "del" "dfn" "dir" "div" "dl" "dt"
                "em" "fieldset" "font" "form" "h1" "h2" "h3" "h4" "h5" "h6" "head"
                "hr" "i" "iframe" "img" "input" "ins" "isindex" "kbd" "label"
                "legend" "li" "link" "map" "menu" "meta" "noframes" "noscript"
                "object" "ol" "optgroup" "option" "p" "param" "pre" "q" "s" "samp"
                "script" "select" "small" "span" "strike" "strong" "style" "sub"
                "sup" "table" "tbody" "td" "textarea" "tfoot" "th" "thead" "title"
                "tr" "tt" "u" "ul" "var"})
(def
  ^{:dynamic true
    :doc "Set of legal HTML attributes that should be part of the search space."}
  *html-attributes* #{"abbr" "accept" "accept-charset" "accesskey" "action" "align"
                      "alink" "alt" "archive" "axis" "background" "bgcolor" "border"
                      "cellpadding" "cellspacing" "char" "charoff" "charset"
                      "checked" "cite" "class" "classid" "clear" "code" "codebase"
                      "codetype" "color" "cols" "colspan" "compact" "content"
                      "coords" "data" "datetime" "declare" "defer" "dir" "disabled"
                      "enctype" "face" "for" "frame" "frameborder" "headers" "height"
                      "href" "hreflang" "hspace" "http-equiv" "id" "ismap" "label"
                      "lang" "language" "link" "longdesc" "marginheight"
                      "marginwidth" "maxlength" "media" "method" "multiple" "name"
                      "nohref" "noshade" "nowrap" "object" "onblur" "onchange"
                      "onclick" "ondblclick" "onfocus" "onkeydown" "onkeypress"
                      "onkeyup" "onload" "onmousedown" "onmousemove" "onmouseout"
                      "onmouseover" "onmouseup" "onreset" "onselect" "onsubmit"
                      "onunload" "profile" "prompt" "readonly" "rel" "rev" "rows"
                      "rowspan" "rules" "scheme" "scope" "scrolling" "selected"
                      "shape" "size" "span" "src" "standby" "start" "style" "summary"
                      "tabindex" "target" "text" "title" "type" "usemap" "valign"
                      "value" "valuetype" "vlink" "vspace" "width"})

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

(defn all-elements
  "Shortcut for using WebDriver to get all elements"
  []
  (wd/find-elements *driver* *search-domain*))

(defn all-raw-elements
  "Shortcut for using Enlive to read source and return all elements"
  []
  (let [tree (get-source *driver*)]
    (h/select tree *raw-search-domain*)))

(defn all-child-elements
  "Shortcut for using WebDriver to get all elements beneath an element"
  [parent-elem]
  (wd/find-elements parent-elem *child-search-domain*))

(defn all-raw-child-elements
  "Shortcut for using Enlive to get all elements beneath an element"
  [parent-elem]
  (let [tree (get-source *driver*)]
    (h/select tree (concat parent-elem *raw-child-search-domain*))))

;; ### Relations ###

;; Time: 769 ms
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

;; Time: 8 ms
(defn raw-attributeo
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
                         (for [element (all-raw-elements)]
                           (unify a
                                  [elem attr value]
                                  [element gattr (get-in element [:attrs gattr])])))
        :default (to-stream
                  (for [element (all-raw-elements)
                        attribute *html-attributes*]
                    (unify a
                           [elem attr value]
                           [element attribute (get-in element [:attrs attribute])])))))))

(defn displayedo [])
(defn enabledo [])
(defn existso [])
(defn intersecto [])
(defn presento [])
(defn selected [])
(defn sizeo [])

;; Time: 200.7 ms
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
                         (for [el (all-elements)
                               a-tag *html-tags*]
                           (unify a
                                  [elem tag]
                                  [el (wd/tag el)])))))))

(defn raw-tago
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
                         (for [el (all-elements)
                               a-tag *html-tags*]
                           (unify a
                                  [elem tag]
                                  [el (:tag el)])))))))

(defn texto [])
(defn valueo [])
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

;; TODO: You can't put q everywhere, `parent-elem` is assumed grounded
;; Time: 13 ms
(defn childo
  "A relation where `child-elem` is a child element of the `parent-elem` element on the current page."
  [child-elem parent-elem]
  (fn [a]
    (to-stream
     (map #(unify a
                  [child-elem parent-elem]
                  [% parent-elem])
          (all-child-elements parent-elem)))))

;; Time: 1.3 ms
(defn raw-childo
  "Same as `childo`, but use the source of the page with Enlive"
  [child-elem parent-elem]
  (fn [a]
    (to-stream
     (map #(unify a
                  [child-elem parent-elem]
                  [% parent-elem])
          (all-raw-child-elements parent-elem)))))

(comment

  (set-driver! {:browser :firefox
                    :cache-spec {:strategy :basic
                                 :args [{}]
                                 :include [ {:xpath "//a"} ]}
                    }
               "https://github.com")

  )