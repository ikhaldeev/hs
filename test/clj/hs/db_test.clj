(ns hs.db-test
  (:require
   [clojure.test :refer [use-fixtures deftest testing is]]
   [clojure.java.jdbc :as jdbc]
   [hs.db :as db])
  (:import [java.time LocalDate]))

(defn with-test-db
  [f]
  (with-redefs [db/pg-db (assoc db/pg-db :dbname (or (System/getenv "DBNAME_TEST") "hs_test"))]
    (f)))

(use-fixtures :once with-test-db)

(deftest test-env
  (testing "can use db in ci"
    (jdbc/with-db-connection [db-con db/pg-db]
      (let [policy-number "000-ABC-111"
            {patient-id :id} (db/insert-patient db-con {:first_name "first name"
                                                        :middle_name "middle name"
                                                        :last_name "last name"
                                                        :sex "male"
                                                        :dob (LocalDate/parse "1987-02-20")
                                                        :address "some address"
                                                        :policy_number policy-number})
            retrieved (db/patient-by-id db-con {:id patient-id})]
        (is (= policy-number (:policy_number retrieved)))))))
