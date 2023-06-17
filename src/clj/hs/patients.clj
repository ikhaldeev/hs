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

(defn get-patient
  [id]
  (-> (db/patient-by-id ds {:id id})
      (update :dob #(.toString %))))
