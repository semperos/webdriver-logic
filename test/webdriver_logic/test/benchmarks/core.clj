(ns webdriver-logic.test.benchmarks.core
  (:refer-clojure :exclude [==])
  (:use webdriver-logic.core
        clojure.core.logic
        criterium.core)
  (:require [webdriver-logic.raw :as raw]))

(def ^:dynamic *default-url* "https://github.com")

(defn start-browser
  []
  (set-driver! {:browser :firefox
                    :cache-spec {:strategy :basic
                                 :args [{}]
                                 :include [ {:xpath "//a"} ]}
                    }
               *default-url*))

(defn quit-browser
  []
  (clj-webdriver.core/quit webdriver-logic.state/*driver*))

;; ## Benchmarks ##
;;
;; This is the print out of my system data for each benchmark.

(comment
"
     x86_64 Mac OS X 10.7.4 8 cpu(s)
     Java HotSpot(TM) 64-Bit Server VM 20.8-b03-424
     Runtime arguments: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n -Dclojure.compile.path=/Users/semperos/dev/clojure/webdriver-logic/classes -Dwebdriver-logic.version=0.0.1-SNAPSHOT -Dclojure.debug=false
"
)

;; ### Individual Functions ###

(comment
"
    Evaluation count             : 240
             Execution time mean : 285.431004 ms  95.0% CI: (285.245250 ms, 285.684354 ms)
    Execution time std-deviation : 27.940544 ms  95.0% CI: (27.790336 ms, 28.050204 ms)
         Execution time lower ci : 239.179000 ms  95.0% CI: (239.179000 ms, 239.179000 ms)
         Execution time upper ci : 332.341750 ms  95.0% CI: (332.341750 ms, 332.405100 ms)
")
(defn bench-tago
  []
  (run 2 [q]
       (tago q :a)))

(comment
"
    Evaluation count             : 12420
             Execution time mean : 4.838575 ms  95.0% CI: (4.838211 ms, 4.838844 ms)
    Execution time std-deviation : 45.045486 us  95.0% CI: (44.396467 us, 45.573489 us)
         Execution time lower ci : 4.789816 ms  95.0% CI: (4.789816 ms, 4.789816 ms)
         Execution time upper ci : 4.894847 ms  95.0% CI: (4.894440 ms, 4.896366 ms)
")
(defn bench-tago-RAW
  []
  (run 2 [q]
       (raw/tago q :a)))

;; ### Multiple Functions ###

(comment
"
     Evaluation count             : 120
              Execution time mean : 769.255192 ms  95.0% CI: (769.030392 ms, 769.496342 ms)
     Execution time std-deviation : 31.689772 ms  95.0% CI: (31.579521 ms, 31.797228 ms)
          Execution time lower ci : 715.613500 ms  95.0% CI: (715.613500 ms, 715.613500 ms)
          Execution time upper ci : 812.841900 ms  95.0% CI: (812.757000 ms, 812.841900 ms)
")
(defn bench-find-el-and-tag-via-attr
  []
  (run 2 [q]
       (fresh [el tag]
              (attributeo el :href "https://github.com/blog")
              (tago el tag)
              (== q [el tag]))))

(comment
"
     Evaluation count             : 11640
              Execution time mean : 5.132349 ms  95.0% CI: (5.132152 ms, 5.132576 ms)
     Execution time std-deviation : 25.492576 us  95.0% CI: (25.256281 us, 25.723954 us)
          Execution time lower ci : 5.097979 ms  95.0% CI: (5.097979 ms, 5.097979 ms)
          Execution time upper ci : 5.187088 ms  95.0% CI: (5.187088 ms, 5.187849 ms)
")
(defn bench-find-el-and-tag-via-attr-RAW
  []
  (run 2 [q]
       (fresh [el tag]
              (raw/attributeo el :href "https://github.com/blog")
              (raw/tago el tag)
              (== q [el tag]))))

(defn run-benchy
  [f]
  (start-browser)
  (report-result (benchmark (f)) :verbose)
  (quit-browser))