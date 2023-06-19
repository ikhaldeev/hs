(ns hs.db-test
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [next.jdbc :as jdbc]
    [next.jdbc.sql :as sql]
    [hs.db :as db])
  (:import [java.time LocalDate]))

(defn with-test-db
  [f]
  (let [pg-db (assoc db/pg-db :dbname (or (System/getenv "DBNAME_TEST") "hs_test"))]
    (with-redefs [db/ds (jdbc/get-datasource pg-db)]
      (f))))

(defn clear-table!
  [table]
  (jdbc/execute! db/ds [(str "delete from " (name table))]))

(use-fixtures :once with-test-db)

(def policy-number "000-ABC-111")
(def test-patient {:first-name "first name"
                   :middle-name "middle name"
                   :last-name "last name"
                   :sex "male"
                   :dob (LocalDate/parse "1987-02-20")
                   :address "some address"
                   :policy-number policy-number})

(deftest test-env
  (testing "can use db in ci"
    (let [policy-number "000-ABC-111"
          {patient-id :id} (db/insert-patient db/ds test-patient)
          retrieved (db/patient-by-id db/ds {:id patient-id})]
      (is (= policy-number (:policy_number retrieved))))))
