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

(defn- ->patient-view
  [patient]
  (update patient :dob #(.toString %)))

(defn get-patient
  [id]
  (-> (db/patient-by-id ds {:id id})
      (->patient-view)))

(defn list-patients
  [data]
  (let [validated-data (v/validated ::v/list-patients data)
        query-data (if (:q validated-data)
                     (update validated-data :q #(map db/patients-q-snip-sqlvec %))
                     validated-data)
        patients (->> (db/list-patients ds query-data)
                      (map ->patient-view))]
    {:patients patients}))
