(ns webdriver-logic.test.core
  (:refer-clojure :exclude [==])
  (:use [webdriver-logic.core]
        [clojure.core.logic :exclude [is]]
        [clojure.test]
        [ring.adapter.jetty :only [run-jetty]])
  (:require [clj-webdriver.core :as wd]
            [webdriver-logic.test.example-app.core :as web-app]))

;; ## Setup ##
(def test-port 5745)
(def test-host "localhost")
(def test-base-url (str "http://" test-host ":" test-port "/"))
(def driver (wd/new-driver {:browser :firefox}))
(set-driver! driver)

;; Fixtures
(defn start-server [f]
  (loop [server (run-jetty #'web-app/routes {:port test-port, :join? false})]
    (if (.isStarted server)
      (do
        (f)
        (.stop server))
      (recur server))))

(defn reset-browser-fixture
  [f]
  (wd/to driver test-base-url)
  (f))

(defn quit-browser-fixture
  [f]
  (f)
  (wd/quit driver))

(use-fixtures :once start-server quit-browser-fixture)
(use-fixtures :each reset-browser-fixture)

(defn go-to-form-page
  []
  (wd/click (wd/find-element driver {:text "example form"})))

;;
;; ### Test Cases ####
;;
;; Yes, these do beg the question.
;;
;; Also, `*search-domain*` gets rebound often due to performance reasons.
;;

(deftest test-basic-logic-success
  (s (run* [q]
           (== q true))))

;; This test doubles as a test of all the s/u test macros.
(deftest test-attributeo
  ;; There is an element with id "pages-table".
  (s (run* [q]
           (attributeo q :id "pages-table")))
  ;; There is no element with id "no-such-id".
  (u (run* [q]
           (attributeo q :id "no-such-id")))
  ;; There are multiple elements with class "external".
  (s+ (run* [q]
            (attributeo q :class "external")))
  ;; All goal values returned from the run equal "class".
  (s? (fn [goal-vals]
        (every? #(= % "class") goal-vals))
      (binding [*search-domain* {:css "#content > p *"}]
        (run 2 [q]
             (fresh [el attr]
                    (attributeo el attr "external")
                    (== q attr)))))
  ;; Of all attributes with value "external", "class" is one.
  (s-includes ["class"]
              (binding [*search-domain* {:css "#content > p *"}]
                (run 2 [q]
                     (fresh [el attr]
                            (attributeo el attr "external")
                            (== q attr)))))
  ;; Only the "class" attribute has a value of "external" for all elements.
  (s-as ["class"]
              (binding [*search-domain* {:css "#content > p *"}]
                (distinct (run 2 [q]
                               (fresh [el attr]
                                      (attributeo el attr "external")
                                      (== q attr))))))
  ;; Alternative syntax for single-value checks.
  (s-as "class"
              (binding [*search-domain* {:css "#content > p *"}]
                (distinct (run 2 [q]
                               (fresh [el attr]
                                      (attributeo el attr "external")
                                      (== q attr))))))
  ;; Id's on the page include "pages" and "pages-table".
  (s-includes ["pages" "pages-table"]
              (binding [*search-domain* {:css "#content > *"}]
                (run* [q]
                      (fresh [el value]
                             (attributeo el :id value)
                             (== q value))))))

(deftest test-childo
  ;; The content div has multiple children.
  (s+ (run 3 [q]
           (childo q (wd/find-element driver {:css "#content"}))))
  ;; A direct child anchor tag of a direct child paragraph tag of the element
  ;; with id `content` has multiple parents.
  (s+ (run 2 [q]
           (childo (wd/find-element driver {:css "#content > p > a"}) q))))

(deftest test-displayedo
  ;; The page contains multiple elements that are displayed (visible).
  (s+ (run 2 [q]
           (displayedo q)))
  ;; The first anchor tag with a class of external is displayed (visible).
  (s (run* [q]
           (displayedo (wd/find-element driver {:css "a.external"})))))

(deftest test-enabledo
  ;; Go to the page with forms on it.
  (go-to-form-page)
  ;; The page contains multiple elements that are enabled.
  (s+ (run 2 [q]
           (enabledo q)))
  ;; The first input element is enabled.
  (s (run* [q]
           (enabledo (wd/find-element driver {:css "input"}))))
  ;; The input element with id `disabled_field` is not enabled.
  (u (run* [q]
           (enabledo (wd/find-element driver {:css "input#disabled_field"})))))

(deftest test-existso
  ;; At least two elements exist.
  (s+ (run 2 [q]
           (existso q)))
  ;; An element with class `external` does exist.
  (s (run* [q]
           (existso (wd/find-element driver {:class "external"}))))
  ;; An element with id `no-such-id` does not exist.
  (u (run 1 [q]
          (existso (wd/find-element driver {:id "no-such-id"})))))

(deftest test-presento
  ;; At least two elements are present.
  (s+ (run 2 [q]
           (presento q)))
  ;; The first element with class `external` is present.
  (s (run* [q]
           (presento (wd/find-element driver {:class "external"}))))
  ;; The first anchor tag with an `href` of `#pages` exists...
  (s (run* [q]
           (existso (wd/find-element driver {:tag :a, :href "#pages"}))))
  ;; ...but is not visible...
  (u (run* [q]
           (displayedo (wd/find-element driver {:tag :a, :href "#pages"}))))
  ;; ...and thus is not present.
  (u (run* [q]
           (presento (wd/find-element driver {:tag :a, :href "#pages"}))))
  ;; And together:
  (is (= (run* [q]
               (existso (wd/find-element driver {:tag :a, :href "#pages"}))
               (displayedo (wd/find-element driver {:tag :a, :href "#pages"})))
         (run* [q]
               (presento (wd/find-element driver {:tag :a, :href "#pages"})))
         ())))

(deftest test-selectedo
  (go-to-form-page)
  ;; #countries option[value='bharat']
  ;; Multiple options in select lists are selected.
  (s+ (run 2 [q]
          (selectedo q)))
  ;; The option element with value `bharat` is selected.
  (s (run* [q]
          (selectedo (wd/find-element driver {:css "#countries option[value='bharat']"}))))
  ;; The option element with value `ayiti` is not selected.
  (u (run* [q]
           (selectedo (wd/find-element driver {:css "#countries option[value='ayiti']"}))))
  (s-includes ["bharat"]
              (run 2 [q]
                   (fresh [the-el the-value]
                          (selectedo the-el)
                          (attributeo the-el :selected "selected")
                          (attributeo the-el :value the-value)
                          (== q the-value)))))



;; TODO:
;;
;;  * sizeo
;;  * tago
;;  * texto
;;  * valueo
;;  * visibleo
;;
;; And then tests combining all relations in various orders with
;; ground and fresh variables in all possible positions.