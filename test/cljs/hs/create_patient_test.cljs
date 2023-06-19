(ns hs.create-patient-test
  (:require [cljs.test :refer-macros [use-fixtures deftest testing is]]
            [re-frame.core :as re-frame]
            [day8.re-frame.test :as rf-test]
            [hs.core :as core]
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

(defn init-page
  []
  (js/document.body.insertAdjacentHTML "afterBegin" "<div id='app'></div>")
  (core/init))

(use-fixtures :once {:before #(init-page)})
(use-fixtures :each {:before #(re-frame/dispatch-sync [::state/set-active-route {:route-name :patients}])})

(deftest create-patient-form-initialized
  (rf-test/run-test-async
   (testing "dom initialized"
     (let [active-route (re-frame/subscribe [::state/active-route])]
       (re-frame/dispatch [::state/open-create-patient-form])
       (rf-test/wait-for [::state/init-create-patient-form]
                         (is (= :create-patient (-> @active-route :route-name))))))))

(deftest create-patient-form
  (rf-test/run-test-sync
   (testing "form data cleaned up on initialing"
     (re-frame/dispatch [::state/set-form-value :first-name #js {:target {:value "name"}}])
     (re-frame/dispatch [::state/init-create-patient-form])

     (is (= {} (-> @re-frame.db/app-db :form))))))
