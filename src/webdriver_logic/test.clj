(ns webdriver-logic.test
  (:use [clojure.pprint :only [pprint]])
  (:require [clojure.test :as test]))

(defmacro s
  "Deterministic test. Deterministic predicates are predicates that must succeed exactly once and, for well behaved predicates, leave no choicepoints.

   This form is not concerned with the actual value returned, just that the run was successful and returned only one value."
  ([run-body] `(s ~run-body false))
  ([run-body print?]
     `(let [goal-values# ~run-body]
        (when ~print?
          (->> (pprint goal-values#) with-out-str (str "Goal output:\n") print))
        (test/is (= (count goal-values#) 1) "This logic program should succeed with exactly one value."))))

(defmacro s+
  "Assert that a run returns more than one value (non-deterministic).

   This form is not concerned with the actual values returned, just that the run was successful and returned more than one value."
  ([run-body] `(s+ ~run-body false))
  ([run-body print?]
     `(let [goal-values# ~run-body]
        (when ~print?
          (->> (pprint goal-values#) with-out-str (str "Goal output:\n") print))
        (test/is (> (count goal-values#) 1) "This logic program should succeed more than once."))))

(defmacro s?
  "Assert that a run returns values that, when passed as a seq of values to `pred`, makes `pred` return a truthy value."
  [pred run-body]
  `(let [goal-values# ~run-body]
     (test/is (~pred goal-values#) "This logic program should succeed and return a seq of values that, when passed to the given predicate, return true.")))

(defmacro u
  "Assert that a run fails."
  [run-body]
  `(let [goal-values# ~run-body]
     (test/is (not (seq goal-values#)) "This logic program should fail.")))

(defmacro s-as
  "Assert that the run is successful and returns a sequence of values equivalent to `coll`. If only a single value is expected, as a convenience `coll` may be this standalone value."
  [coll run-body]
  `(let [goal-values# ~run-body
         a-coll# (if (and (coll? ~coll)
                          (not (map? ~coll)))
                   ~coll
                   '(~coll))]
     (test/is (= a-coll# goal-values#) "This logic program should succeed and return exactly the values provided.")))

(defmacro s-includes
  "Assert that the run is successful and that the items in `coll` are included in the return value. The items in `coll` need not be exhaustive; the assertion only fails if one of the items in `coll` is not returned from the run."
  [coll run-body]
  `(let [goal-values# ~run-body
         a-coll# (if (and (coll? ~coll)
                          (not (map? ~coll)))
                   ~coll
                   '(~coll))]
     (test/is (not (some nil?
                      (map #(some #{%} goal-values#) a-coll#)))
              "This logic program should succeed and the values returned should include the given values.")))