(ns hs.db
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [next.jdbc :as jdbc]))

(def pg-db {:dbtype "postgresql"
            :dbname (or (System/getenv "DBNAME") "hs")
            :host (or (System/getenv "DBHOST") "localhost")
            :user (or (System/getenv "DBUSER") "hs")
            :password (or (System/getenv "DBPASSWORD") "hs")})

(def ds (jdbc/get-datasource pg-db))

(hugsql/def-db-fns "sql/queries.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc jdbc/unqualified-snake-kebab-opts)})

(comment
  
  (insert-patient ds {:first-name "first name"
                      :middle-name "middle name"
                      :last-name "last name"
                      :sex "male"
                      :dob (java.time.LocalDate/parse "1987-02-20")
                      :address "some address"
                      :policy-number "000-ABC-111"})
  (-> (patient-by-id ds {:id 3})
      (update :dob #(.toString %)))
  )

