(defproject webdriver-logic "0.0.1-SNAPSHOT"
  :description "Library for composing clj-webdriver tests using logic programming"
  :url "https://github.com/semperos/webdriver-logic"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/core.logic "0.8-alpha3"]
                 [clj-webdriver "0.6.0-beta1"]
                 [enlive "1.0.1"]]
  :profiles {:dev
             {:dependencies
              [[criterium "0.2.1"]
               [ring "1.1.1"]
               [enlive "1.0.1"]
               [net.cgrand/moustache "1.1.0"]]}}
  :scm {:url "git@github.com:semperos/webdriver-logic.git"})