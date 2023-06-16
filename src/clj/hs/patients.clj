(ns hs.patients
  (:require [hs.db :refer [ds] :as db])
  (:import [java.time LocalDate]))

(defn validate!
  [spec data])

(defn create-patient
  [data]
  (validate! ::create-patient data)
  (let [[result] (db/insert-patient ds (-> data
                                           (update :dob #(LocalDate/parse %))))]
    result))

(defn get-patient
  [id]
  (-> (db/patient-by-id ds {:id id})
      (update :dob #(.toString %))))
