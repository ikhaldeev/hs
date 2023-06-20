(ns hs.edit-patient-test
  (:require [cljs.test :refer-macros [use-fixtures deftest testing is]]
            [re-frame.core :as re-frame]
            [day8.re-frame.test :as rf-test]
            [hs.fixtures :as f]
            [hs.state :as state]))

(use-fixtures :once {:before #(f/init-page)})
(use-fixtures :each {:before #(re-frame/dispatch-sync [::state/set-active-route {:route-name :patients}])})

(deftest edit-patient-form-initialized
  (rf-test/run-test-async
   (testing "patient info loaded on open page"
     (let [patient-id 1
           patient-data (assoc f/test-patient :id patient-id)
           form-data (re-frame/subscribe [::state/form-data])]
       (f/mock-warehouse {(str "/api/patients/" patient-id) patient-data})
       (re-frame/dispatch [::state/open-edit-patient-form {:patient-id patient-id}])
       (rf-test/wait-for [::state/load-patient-success]
                         (is (= patient-data @form-data)))))))

(deftest edit-patient-form-redirect
  (rf-test/run-test-sync
   (testing "form data saved and page redirects to patients list"
     (let [patient-id 1
           patient-data (assoc f/test-patient :id patient-id)
           edited-name "EditedName"
           form-data (re-frame/subscribe [::state/form-data])
           active-route (re-frame/subscribe [::state/active-route])]
       (f/mock-warehouse {(str "/api/patients/" patient-id) patient-data})
       (re-frame/dispatch [::state/init-edit-patient {:patient-id patient-id}])
       (re-frame/dispatch [::state/set-form-value :first-name edited-name])

       (is (= edited-name (-> @form-data :first-name)))

       (re-frame/dispatch [::state/edit-patient])
       
       (is (= :patients (-> @active-route :route-name)))))))
