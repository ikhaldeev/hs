(ns hs.validation
  (:require [clojure.spec.alpha :as s]))

(s/def ::not-empty seq)
(s/def ::first-name (s/and ::not-empty string?))
(s/def ::middle-name (s/and string?))
(s/def ::last-name (s/and seq string?))
(s/def ::sex #{"male" "female" "other"})
(def dob-regex #"^\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$")
(s/def ::dob (s/and string? #(re-matches dob-regex %)))
(s/def ::policy-number (s/and seq string?))
(s/def ::address (s/and seq string?))

(s/def ::create-patient
  (s/keys :req-un [::first-name ::last-name ::sex ::dob ::policy-number]
          :opt-un [::middle-name ::address]))

(s/def ::edit-patient
  (s/keys :req-un [::first-name ::last-name ::sex ::dob ::policy-number]
          :opt-un [::middle-name ::address]))

(s/def ::list-patients
  (s/keys :opt-un [::q ::dob-start ::dob-end ::sexes ::policy-number-starts]))

(defn- get-problem-field
  [problem]
  (let [path (:path problem)
        predicate (:pred problem)
        get-field-keyword #(when (sequential? %) (->> % (last) (last)))]
    (or (first path) (get-field-keyword predicate))))

(defn get-errors
  [spec data]
  (let [explain-data (s/explain-data spec data)
        problems (or (:clojure.spec.alpha/problems explain-data)
                     (:cljs.spec.alpha/problems explain-data))]
    (println explain-data)
    (->> problems
         (map (fn [p] {:field (get-problem-field p)
                       :via (some->> p :via (drop 1) (last) (name) (keyword))})))))

(defn- select-only-defined-keys
  [spec data]
  (let [[_ req _ opt] (->> (s/describe spec)
                           (drop 1))
        keys-to-select (->> (concat req opt)
                            (map name)
                            (map keyword))]
    (select-keys data keys-to-select)))

(defn validated
  [spec data]
  (let [prepared-data (select-only-defined-keys spec data)]
    (when (not (s/valid? spec prepared-data))
      (throw (ex-info "Validation failed"
                      {:type :validation
                       :errors (get-errors spec data)})))
    prepared-data))

(comment
  (s/valid? ::first-name "na")
  (s/valid? ::first-name "")
  (s/valid? ::first-name nil)
  (s/valid? ::sex "something")
  (s/valid? ::sex "male")
  (s/valid? ::dob "1987-02-20")
  (s/valid? ::dob "@987-02-20")
  (s/valid? ::dob "1987-20-20")
  (s/valid? ::dob "1987-20")
  (s/valid? ::create-patient {:first-name "Ivan"
                              :middle-name "Sergeevich"
                              :last-name "Khaldeev"
                              :sex "male"
                              :dob "1987-02-20"
                              :policy-number "1234-ABC-1234"
                              :address "city, zip, street"})

  (let [[_ req _ opt] (->> (s/describe ::create-patient)
                           (drop 1))]
    (->> (concat req opt)
         (map name)
         (map keyword)))

  (get-errors ::create-patient {:first-name "Ivan"
                                :middle-name "Sergeevich"
                                :last-name "Khaldeev"
                                :dob "asdf"
                                :sex "m"})

  (get-errors ::create-patient {:first-name ""
                                :middle-name ""
                                :last-name "Khaldeev"
                                :sex "male"
                                :dob "1987-02-20"
                                :policy-number "1234-ABC-1234"
                                :address "city, zip, street"})

  (get-errors ::create-patient {:first-name "Ivan"
                                :last-name "Khaldeev"
                                :sex "male"
                                :dob "1987-02-20"
                                :policy-number "1234-ABC-1234"
                                :address "city, zip, street"}))
