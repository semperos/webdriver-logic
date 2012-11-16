(ns ^{:doc "Benchmarks performed with criterium. Where quick-benchmark was used, this is noted."}
  webdriver-logic.test.benchmarks.core
  (:refer-clojure :exclude [==])
  (:use webdriver-logic.core
        clojure.core.logic
        [clj-webdriver.core :only [quit start]]
        criterium.core)
  (:require [webdriver-logic.raw :as raw]))

(def ^:dynamic *default-url* "https://github.com")

(defn start-browser
  []
  (let [d (start {:browser :chrome

                  } *default-url*)]
    (set-driver! d)
    (raw/set-driver! d))
  )

(defn quit-browser
  []
  (quit *driver*))

;; ## Benchmarks ##
;;
;; This is the print out of my system data for each benchmark.

(comment
"
x86_64 Mac OS X 10.8.2 8 cpu(s)
Java HotSpot(TM) 64-Bit Server VM 23.3-b01
Runtime arguments: -XX:+TieredCompilation -agentlib:jdwp=transport=dt_socket,server=y,suspend=n -Dclojure.compile.path=/Users/semperos/dev/clojure/webdriver-logic/target/classes -Dwebdriver-logic.version=0.0.1-SNAPSHOT -Dclojure.debug=false
"
)

;; ### Individual Functions ###

(comment
"
NOTE: The quick-benchmark function was used.
Evaluation count             : 6
             Execution time mean : 783.479833 ms  95.0% CI: (781.625833 ms, 784.059000 ms)
    Execution time std-deviation : 43.967869 ms  95.0% CI: (43.643979 ms, 44.710451 ms)
         Execution time lower ci : 730.513000 ms  95.0% CI: (730.513000 ms, 730.513000 ms)
         Execution time upper ci : 838.903500 ms  95.0% CI: (837.897000 ms, 838.903500 ms)

Found 1 outliers in 6 samples (16.6667 %)
	low-severe	 1 (16.6667 %)
 Variance from outliers : 14.3697 % Variance is moderately inflated by outliers
")
(defn bench-tago
  []
  (run 2 [q]
       (tago q :a)))

(comment
"
NOTE: The quick-benchmark function was used.
Evaluation count             : 156
             Execution time mean : 4.036173 ms  95.0% CI: (4.034782 ms, 4.038301 ms)
    Execution time std-deviation : 108.084995 us  95.0% CI: (105.703304 us, 110.306433 us)
         Execution time lower ci : 3.951577 ms  95.0% CI: (3.951577 ms, 3.951577 ms)
         Execution time upper ci : 4.150712 ms  95.0% CI: (4.150712 ms, 4.176962 ms)
Improvement: ~195 times
")
(defn bench-tago-RAW
  []
  (run 2 [q]
       (raw/tago q :a)))

;; ### Multiple Functions ###

(comment
"
NOTE: The quick-benchmark function was used.
Evaluation count             : 6
             Execution time mean : 1.178506 sec  95.0% CI: (1.177556 sec, 1.179908 sec)
    Execution time std-deviation : 65.447901 ms  95.0% CI: (64.942283 ms, 65.675600 ms)
         Execution time lower ci : 1.123121 sec  95.0% CI: (1.123121 sec, 1.123121 sec)
         Execution time upper ci : 1.245426 sec  95.0% CI: (1.245426 sec, 1.260944 sec)
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
NOTE: The quick-benchmark function was used.
Evaluation count             : 84
             Execution time mean : 7.705119 ms  95.0% CI: (7.700595 ms, 7.709786 ms)
    Execution time std-deviation : 1.046114 ms  95.0% CI: (1.043498 ms, 1.047432 ms)
         Execution time lower ci : 7.186214 ms  95.0% CI: (7.186214 ms, 7.186214 ms)
         Execution time upper ci : 9.169911 ms  95.0% CI: (9.169911 ms, 9.250625 ms)

Found 1 outliers in 6 samples (16.6667 %)
	low-severe	 1 (16.6667 %)
 Variance from outliers : 31.7636 % Variance is moderately inflated by outliers
Improvement: ~168 times
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

(defn run-quick-benchy
  [f]
  (start-browser)
  (report-result (quick-benchmark (f)) :verbose)
  (quit-browser))