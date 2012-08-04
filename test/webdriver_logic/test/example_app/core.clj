(ns webdriver-logic.test.example-app.core
  (:use net.cgrand.moustache
        webdriver-logic.test.example-app.templates
        [ring.util.response :only [response]]))

(defn view-frontpage
  [r]
  (->> (page nil nil (welcome-page))
       response))

(defn view-clojure-page
  [r]
  (->> (page nil nil (clojure-page))
       response))

(defn view-example-form
  [r]
  (->> (page nil nil (example-form))
       response))

(def routes
  (app
   [""] view-frontpage
   ["clojure"] view-clojure-page
   ["example-form"] view-example-form))