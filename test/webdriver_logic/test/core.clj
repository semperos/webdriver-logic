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

;;
;; ### Test Cases ####
;;
;; Yes, these do beg the question.
;;
;; Also, `*search-domain*` get rebound often due to performance reasons.
;;

(deftest test-basic-logic-success
  (s (run* [q]
           (== q true))
     :print))

;; This test doubles as a test of all the s/u test macros
(deftest test-attributeo
  ;; There is an element with id "pages-table"
  (s (run* [q]
           (attributeo q :id "pages-table")))
  ;; There is no element with id "no-such-id"
  (u (run* [q]
           (attributeo q :id "no-such-id")))
  ;; There are multiple elements with class "external"
  (s+ (run* [q]
            (attributeo q :class "external")))
  ;; All goal values returned from the run equal "class"
  (s? (fn [goal-vals]
        (every? #(= % "class") goal-vals))
      (binding [*search-domain* {:css "#content > p *"}]
        (run 2 [q]
             (fresh [el attr]
                    (attributeo el attr "external")
                    (== q attr)))))
  ;; Of all attributes with value "external", "class" is one
  (s-includes ["class"]
              (binding [*search-domain* {:css "#content > p *"}]
                (run 2 [q]
                     (fresh [el attr]
                            (attributeo el attr "external")
                            (== q attr)))))
  ;; Only the "class" attribute has a value of "external" for all elements
  (s-as ["class"]
              (binding [*search-domain* {:css "#content > p *"}]
                (distinct (run 2 [q]
                               (fresh [el attr]
                                      (attributeo el attr "external")
                                      (== q attr))))))
  ;; Alternative syntax for single-value checks
  (s-as "class"
              (binding [*search-domain* {:css "#content > p *"}]
                (distinct (run 2 [q]
                               (fresh [el attr]
                                      (attributeo el attr "external")
                                      (== q attr))))))
  ;; Id's on the page include "pages" and "pages-table"
  (s-includes ["pages" "pages-table"]
              (binding [*search-domain* {:css "#content > *"}]
                (run* [q]
                      (fresh [el value]
                             (attributeo el :id value)
                             (== q value))))))

(deftest test-childo
  (s+ (run 3 [q]
           (childo q (wd/find-element driver {:css "#content"}))))
  (s+ (run 2 [q]
           (childo (wd/find-element driver {:css "#content > p > a"}) q))))