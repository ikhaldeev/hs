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

(defn patients-q-snip-sqlvec
  [q]
  ["and first_name || ' ' || coalesce(middle_name::text ,'') || ' ' || last_name || ' ' || dob || ' ' || coalesce(address::text ,'') || ' ' || policy_number iLIKE ?", (str "%" q "%")])

(comment
  (hugsql/def-sqlvec-fns "sql/queries.sql"
    {:adapter (next-adapter/hugsql-adapter-next-jdbc jdbc/unqualified-snake-kebab-opts)})
  
  (insert-patient ds {:first-name "Ivan"
                      :middle-name "middle name"
                      :last-name "last name"
                      :sex "male"
                      :dob (java.time.LocalDate/parse "1987-02-20")
                      :address "some address"
                      :policy-number "000-ABC-111"})

  (update-patient ds {:id 11
                      :first-name "Edited"
                      :middle-name "middle name"
                      :last-name "last name"
                      :sex "male"
                      :dob (java.time.LocalDate/parse "1987-02-20")
                      :address "some address"
                      :policy-number "000-ABC-111"})
  
  (-> (patient-by-id ds {:id 3})
      (update :dob #(.toString %)))

  {:q
   [["and first_name || ' ' || middle_name || ' ' || last_name || ' ' || dob || ' ' || address || ' ' || policy_number LIKE ?"
     "%first%"]
    ["and first_name || ' ' || middle_name || ' ' || last_name || ' ' || dob || ' ' || address || ' ' || policy_number LIKE '?'"
     "%address%"]]}
  
  (list-patients ds (-> {:q ["first" "address"]}
                        (update :q #(map patients-q-snip-sqlvec %))))

  (let [search-data {:q ["first" "address"]
                     :dob-start "1986-01-01"
                     :sexes ["male", "other"]}]
    (list-patients ds (cond-> search-data
                              (:q search-data)
                              (update :q #(map patients-q-snip-sqlvec %))

                              (:dob-start search-data)
                              (update :dob-start #(java.time.LocalDate/parse %))

                              (:dob-end search-data)
                              (update :dob-end #(java.time.LocalDate/parse %))
                              
                              (:policy-number-starts search-data)
                              (update :policy-number-starts #(str % "%")))))

  (list-patients ds {:q []})

  (list-patients ds {:count true :offset 10})
  
  (list-patients ds {:q '(["and first_name || ' ' || middle_name || ' ' || last_name || ' ' || dob || ' ' || address || ' ' || policy_number LIKE ?" "%first%"]
                          ["and first_name || ' ' || middle_name || ' ' || last_name || ' ' || dob || ' ' || address || ' ' || policy_number LIKE ?" "%address%"])})

  )

