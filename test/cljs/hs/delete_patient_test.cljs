(ns hs.delete-patient-test
  (:require [cljs.test :refer-macros [use-fixtures deftest testing is]]
            [re-frame.core :as re-frame]
            [day8.re-frame.test :as rf-test]
            [hs.fixtures :as f]
            [hs.state :as state]))

(use-fixtures :once {:before #(f/init-page)})
(use-fixtures :each {:before #(re-frame/dispatch-sync [::state/set-active-route {:route-name :patients}])})

(deftest delete-patient-dialog-initialized
  (rf-test/run-test-sync
   (testing "form data saved and page redirects to patients list"
     (let [patient-id 1
           patient-data (assoc f/test-patient :id patient-id)]
       (f/mock-warehouse {(str "/api/patients") {:patients [patient-data]
                                                  :pages 1}})
       (re-frame/dispatch [::state/init-list-patients])

       (let [first-patient @(re-frame/subscribe [::state/patients])]
         (re-frame/dispatch [::state/show-delete-patient-dialog first-patient])

         (is (= first-patient @(re-frame/subscribe [::state/patient-to-delete]))))))))
