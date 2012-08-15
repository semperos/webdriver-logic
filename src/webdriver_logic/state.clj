(ns ^{:doc "Namespace for managing state required by multiple namespaces."}
  webdriver-logic.state
  (:use [clj-webdriver.driver :only [driver?]])
  (:require [clj-webdriver.core :as wd]))

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