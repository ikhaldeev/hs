(ns hs.patients
  (:require [hs.db :refer [ds] :as db]
            [hs.validation :as v])
  (:import [java.time LocalDate]))

(defn create-patient
  [data]
  (let [defaults {:middle-name nil
                  :address nil}
        validated-data (v/validated ::v/create-patient data)
        [result] (db/insert-patient ds (-> (merge defaults validated-data)
                                           (update :dob #(LocalDate/parse %))))]
    result))

(defn edit-patient
  [id data]
  (let [defaults {:middle-name nil
                  :address nil}
        validated-data (v/validated ::v/edit-patient data)]
    (db/update-patient ds (-> (merge defaults validated-data {:id id})
                              (update :dob #(LocalDate/parse %))))
    {:id id}))

(defn delete-patient
  [id]
  (db/delete-patient ds {:id id}))

(defn- ->patient-view
  [patient]
  (update patient :dob #(.toString %)))

(defn get-patient
  [id]
  (when-let [patient (db/patient-by-id ds {:id id})]
    (->patient-view patient)))

(defn- prepare-query
  [data]
  (cond-> data
          (:q data)
          (update :q #(if (string? %) [%] %))

          (:q data)
          (update :q #(map db/patients-q-snip-sqlvec %))

          (:dob-start data)
          (update :dob-start #(java.time.LocalDate/parse %))

          (:dob-end data)
          (update :dob-end #(java.time.LocalDate/parse %))

          (:policy-number-starts data)
          (update :policy-number-starts #(str % "%"))

          (:sexes data)
          (update :sexes #(if (string? %) [%] %))))

(defn list-patients
  [data]
  (let [validated-data (v/validated ::v/list-patients data)
        query-data (prepare-query validated-data)
        patients (->> (db/list-patients ds query-data)
                      (map ->patient-view))]
    {:patients patients}))

(comment
  (list-patients {:q ["first" "address"]
                  :dob-start "1986-01-01"
                  :sexes ["male", "other"]
                  :policy-number-starts "000"})
  (list-patients {})
  (list-patients {:q ["non-existent"]})
  (list-patients {:q ""})
  (list-patients {:q "first"})
  (list-patients {:dob-start "2050-01-01"})
  (list-patients {:dob-start "1986-01-01"})

  (prepare-query {:dob-end "1900-01-01"})
  (list-patients {:dob-end "1900-01-01"})
  (list-patients {:sexes "female"})
  (list-patients {:sexes ["female"]})
  (list-patients {:sexes ["other"]})
  (list-patients {:policy-number-starts "000"})
  (list-patients {:policy-number-starts "111"})
  (get-patient 18))

