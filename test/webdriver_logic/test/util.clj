(ns webdriver-logic.test.util
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:require [webdriver-logic.test.example-app.core :as web-app]))

(def test-port 5745)
(def test-host "localhost")
(def test-base-url (str "http://" test-host ":" test-port "/"))

(defn start-server [f]
  (loop [server (run-jetty #'web-app/routes {:port test-port, :join? false})]
    (if (.isStarted server)
      (do
        (f)
        (.stop server))
      (recur server))))