(ns hs.test-runner
  (:require
    [clojure.test :refer [run-tests successful?]]
    [hs.handler-test]
    [hs.patients-test]))

(defn run
  [_args]
  (let [result (run-tests 'hs.handler-test 'hs.patients-test)
        exit-code (if (successful? result) 0 1)]
    (System/exit exit-code)))
