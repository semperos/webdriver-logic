(defproject webdriver-logic "0.0.1-SNAPSHOT"
  :description "Library for composing clj-webdriver tests using logic programming"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/core.logic "0.8-alpha2"]
                 [clj-webdriver "0.6.0-alpha11"]
                 [enlive "1.0.1"]]
  :dev-dependencies [[criterium "0.2.1"]
                     [ring "1.1.1"]
                     [enlive "1.0.1"]
                     [net.cgrand/moustache "1.1.0"]])