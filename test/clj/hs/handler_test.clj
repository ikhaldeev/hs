(ns hs.handler-test
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [ring.mock.request :as mock]
    [hs.db-test :refer [with-test-db clear-table!]]
    [hs.handler :refer [dev-handler]]
    [hs.patients :as p]
    [cheshire.core :as json]))

(use-fixtures :once with-test-db)

(def policy-number "000-ABC-111")
(def test-patient {:first-name "first name"
                   :middle-name "middle name"
                   :last-name "last name"
                   :sex "male"
                   :dob "1987-02-20"
                   :address "some address"
                   :policy-number policy-number})

(defn- patient-created
  [options]
  (let [default test-patient
        data (merge default options)]
    (p/create-patient data)))

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
