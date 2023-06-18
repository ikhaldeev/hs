(ns hs.create-patient-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as re-frame]
            [day8.re-frame.test :as rf-test]
            [hs.state :as state]))

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

(deftest create-patient-form
  (rf-test/run-test-async
   (testing "form data cleaned up on initialing. really dumb test, because we manually invoke init event"
     (let [request-body (atom nil)]
       (mock-warehouse {"/api/patients" (fn [{form-data :params}]
                                          (reset! request-body form-data)
                                          {:id 1})})
       (re-frame/dispatch [::state/open-create-patient-form])
       (re-frame/dispatch [::state/init-create-patient-form])
       (re-frame/dispatch [::state/set-form-value :first-name #js {:target {:value "name"}}])
       (re-frame/dispatch [::state/cancel-create-patient])
       (re-frame/dispatch [::state/open-create-patient-form])
       (re-frame/dispatch [::state/init-create-patient-form])
       (re-frame/dispatch [::state/create-patient])
       (rf-test/wait-for [::state/create-patient-success]
                         (is (= {} @request-body)))))))
