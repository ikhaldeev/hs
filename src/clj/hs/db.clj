(ns hs.db
  (:require [hugsql.core :as hugsql]
            [clojure.java.jdbc :as jdbc])
  (:import [java.time LocalDate]))

(def pg-db {:dbtype "postgresql"
            :dbname (or (System/getenv "DBNAME") "hs")
            :host (or (System/getenv "DBHOST") "localhost")
            :user (or (System/getenv "DBUSER") "hs")
            :password (or (System/getenv "DBPASSWORD") "hs")})

(hugsql/def-db-fns "sql/queries.sql")

(comment
  (jdbc/with-db-connection [db-con pg-db]
    (insert-patient db-con {:first_name "first name"
                            :middle_name "middle name"
                            :last_name "last name"
                            :sex "male"
                            :dob (LocalDate/parse "1987-02-20")
                            :address "some address"
                            :policy_number "000-ABC-111"}))

  (jdbc/with-db-connection [db-con pg-db]
    (patient-by-id db-con {:id 3})))

