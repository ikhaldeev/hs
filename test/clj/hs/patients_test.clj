(ns hs.patients-test
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [hs.db-test :refer [with-test-db clear-table! patient-created]]
    [hs.patients :as p]))

(use-fixtures :once with-test-db)

(deftest list-patients
  (clear-table! :patients)
  (patient-created {:last-name "White"
                    :policy-number "000-ABC-001"})
  (patient-created {:last-name "Blue"
                    :policy-number "000-ABC-002"})
  (patient-created {:last-name "Red"
                    :sex "female"
                    :policy-number "000-ABC-003"})
  (patient-created {:middle-name nil
                    :address nil
                    :policy-number "000-ABC-004"})
  (testing "result with multiple filters"
    (is (seq (p/list-patients {:q ["first" "address"]
                               :dob-start "1986-01-01"
                               :sexes ["male", "other"]
                               :policy-number-starts "000"}))))
  (testing "no result when query not found"
    (is (empty? (:patients (p/list-patients {:q ["non-existent"]})))))
  (testing "filter by dob start"
    (is (empty? (:patients (p/list-patients {:dob-start "2050-01-01"}))))
    (is (seq (:patients (p/list-patients {:dob-start "1986-01-01"})))))
  (testing "filter by dob end"
    (is (seq (:patients (p/list-patients {:dob-end "2050-01-01"}))))
    (is (empty? (:patients (p/list-patients {:dob-end "1900-01-01"})))))
  (testing "filter by sexes"
    (is (seq (:patients (p/list-patients {:sexes ["female"]}))))
    (is (empty? (:patients (p/list-patients {:sexes ["other"]})))))
  (testing "filter by policy number"
    (is (seq (:patients (p/list-patients {:policy-number-starts "000"}))))
    (is (empty? (:patients (p/list-patients {:policy-number-starts "111"})))))
  (testing "empty string in q parsed as empty filter, and returns everything"
    (is (seq (:patients (p/list-patients {:q ""})))))
  (testing "q should work with nil values for middle name and address"
    (is (= 4 (count (:patients (p/list-patients {:q ""}))))))
  (testing "string in q parsed as single query string"
    (is (seq (:patients (p/list-patients {:q "red"})))))
  (testing "string in sexes parsed as single query string. handle http query of form ?sex=male and ?sex=female&sex=male"
    (is (seq (:patients (p/list-patients {:sexes "male"}))))))
