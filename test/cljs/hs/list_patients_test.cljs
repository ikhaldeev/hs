(ns hs.list-patients-test
  (:require [cljs.test :refer-macros [use-fixtures deftest testing is]]
            [re-frame.core :as re-frame]
            [day8.re-frame.test :as rf-test]
            [hs.fixtures :as f]
            [hs.state :as state]))

(use-fixtures :once {:before #(f/init-page)})
(use-fixtures :each {:before #(re-frame/dispatch-sync [::state/set-active-route {:route-name :patients}])})

(deftest list-patients-form-initialized
  (rf-test/run-test-async
   (testing "patients loaded on open page"
     (f/mock-warehouse {"/api/patients" {:patients [f/test-patient
                                                    f/test-patient]
                                         :pages 1}})
     (let [patients (re-frame/subscribe [::state/patients])]
       (re-frame/dispatch [::state/init-list-patients])
       (rf-test/wait-for [::state/list-patients-success]
                         (is (= 2 (-> @patients :patients count))))))))
