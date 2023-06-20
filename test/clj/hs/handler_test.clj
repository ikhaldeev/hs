(ns hs.handler-test
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [ring.mock.request :as mock]
    [hs.db-test :refer [with-test-db clear-table! test-patient patient-created]]
    [hs.handler :refer [dev-handler]]
    [cheshire.core :as json]))

(use-fixtures :once with-test-db)

(deftest create-patient
  (testing "patient can be created"
    (let [request (-> (mock/request :post "/api/patients")
                      (mock/json-body test-patient))
          response (-> (dev-handler request) :body (json/parse-string true))]
      (is (-> response :id nil? not))))
  (testing "patient can be created without optional fields"
    (let [request (-> (mock/request :post "/api/patients")
                      (mock/json-body (dissoc test-patient :middle-name :address)))
          response (-> (dev-handler request) :body (json/parse-string true))]
      (is (-> response :id nil? not))))
  (testing "errors returned when data is not valid"
    (let [request (-> (mock/request :post "/api/patients")
                      (mock/json-body (dissoc test-patient :first-name)))
          response (-> (dev-handler request))
          response-status (:status response)
          response-body (-> response :body (json/parse-string true))]
      (is (= 400 response-status))
      (is (= nil (-> response-body :errors first :via)))
      (is (= "first-name" (-> response-body :errors first :field))))))

(deftest get-patient
  (testing "created patient can be retrieved"
    (let [{patient-id :id} (-> (dev-handler (-> (mock/request :post "/api/patients")
                                                (mock/json-body test-patient)))
                               :body (json/parse-string true))
          request (-> (mock/request :get (str "/api/patients/" patient-id)))
          response (-> (dev-handler request) :body (json/parse-string true))]
      (is (= patient-id (:id response))))))

(deftest list-patients
  (clear-table! :patients)
  (patient-created {:last-name "White"
                    :policy-number "000-ABC-001"})
  (patient-created {:last-name "Blue"
                    :policy-number "000-ABC-002"})
  (patient-created {:last-name "Red"
                    :policy-number "000-ABC-003"})
  (testing "patients list can be returned"
    (let [request (-> (mock/request :get (str "/api/patients")))
          response (-> (dev-handler request) :body (json/parse-string true))]
      (is (= 3 (-> response :patients count)))))
  (testing "patients list can be searched"
    (let [request (-> (mock/request :get (str "/api/patients") {:q ["white"]}))
          response (-> (dev-handler request) :body (json/parse-string true))]
      (is (= 1 (-> response :patients count))))))

(deftest edit-patient
  (testing "patient can be edited"
    (let [original-data test-patient
          edited-data (assoc test-patient
                             :first-name "Editedname"
                             :last-name "Editedlastname")
          {patient-id :id} (patient-created original-data)
          _ (-> (mock/request :put (str "/api/patients/" patient-id))
                (mock/json-body edited-data)
                (dev-handler))
          request (-> (mock/request :get (str "/api/patients/" patient-id)))
          response (-> (dev-handler request) :body (json/parse-string true))]
      (is (= edited-data (-> response (dissoc :id)))))))

(deftest delete-patient
  (testing "patient can be deleted"
    (let [{patient-id :id} (patient-created)
          before-delete-status(-> (mock/request :get (str "/api/patients/" patient-id))
                                  (dev-handler)
                                  :status)
          delete-status(-> (mock/request :delete (str "/api/patients/" patient-id))
                           (dev-handler)
                           :status)
          after-delete-status(-> (mock/request :get (str "/api/patients/" patient-id))
                                 (dev-handler)
                                 :status)]
      (is (= 200 before-delete-status))
      (is (= 204 delete-status))
      (is (= 404 after-delete-status)))))
