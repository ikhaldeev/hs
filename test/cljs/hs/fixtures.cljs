(ns hs.fixtures
  (:require
    [re-frame.core :as re-frame]
    [hs.core :as core]))

(defn mock-warehouse
  [requests]
  (re-frame/clear-fx :http-xhrio)
  (re-frame/reg-fx
   :http-xhrio
   (fn [{:keys [uri on-success _on-failure] :as request}]
     (when-let [response (get requests uri)]
       (if (fn? response)
         (re-frame/dispatch (conj on-success (response request)))
         (re-frame/dispatch (conj on-success response)))))))

(defn init-page
  []
  (js/document.body.insertAdjacentHTML "afterBegin" "<div id='app'></div>")
  (core/init))

(def test-patient
  {:first-name "first name"
   :middle-name "middle name"
   :last-name "last name"
   :sex "male"
   :dob "1987-02-20"
   :address "some address"
   :policy-number "000-ABC-123"})
