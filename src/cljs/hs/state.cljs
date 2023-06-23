(ns hs.state
  (:require
    [clojure.string :as s]
    [re-frame.core :as re-frame]
    [hs.state.api :as api]
    [hs.validation :as v]))

(defn- prepare-errors
  [errors]
  (->> errors
       (map (juxt #(-> % :field keyword) identity))
       (into {})))

(re-frame/reg-sub
  ::active-route
  (fn [db]
    (:active-route db)))

(re-frame/reg-event-fx
  ::set-active-route
  (fn [{:keys [db]} [_ route]]
    {:db (assoc db :active-route route)}))

(re-frame/reg-event-fx
  ::open-create-patient-form
  (fn [{:keys [_db]} [_]]
    {:dispatch [::set-active-route {:route-name :create-patient}]}))

(re-frame/reg-event-fx
  ::cancel-create-patient
  (fn [{:keys [_db]} [_]]
    {:dispatch [::set-active-route {:route-name :patients}]}))

(re-frame/reg-event-fx
  ::init-create-patient-form
  (fn [{:keys [db]} [_]]
    {:db (-> db
             (assoc :form {})
             (dissoc :form-errors))}))

(re-frame/reg-event-fx
  ::set-form-value
  (fn [{:keys [db]} [_ field value]]
    {:db (assoc-in db [:form field] value)}))

(re-frame/reg-sub
  ::form-errors
  (fn [db]
    (:form-errors db)))

(re-frame/reg-sub
  ::form-data
  (fn [db]
    (:form db)))

(re-frame/reg-event-fx
  ::create-patient
  (fn [{:keys [db]} [_]]
    (let [form-data (:form db)]
      (if (v/valid? ::v/create-patient form-data)
        {:db (-> db
                 (assoc :loading true)
                 (dissoc :form-errors))
         :dispatch [::api/create-patient
                    form-data
                    {:on-success [::create-patient-success]
                     :on-failure [::create-patient-failure]}]}
        (let [errors (prepare-errors (v/get-errors ::v/create-patient form-data))]
          {:db (assoc db :form-errors errors)})))))

(re-frame/reg-event-fx
  ::create-patient-success
  (fn [{:keys [db]} [_ _result]]
    {:db (-> db
             (assoc :loading false)
             (assoc :form {}))
     :dispatch [::set-active-route {:route-name :patients}]}))

(re-frame/reg-event-fx
  ::create-patient-failure
  (fn [{:keys [db]} [_ {{errors :errors} :response}]]
    {:db (-> db
             (assoc :loading false)
             (assoc :form-errors (prepare-errors errors)))}))

(re-frame/reg-event-fx
  ::init-list-patients
  (fn [{:keys [db]} [_]]
    {:db (assoc db :limits {:page 1
                            :page-size 10})
     :dispatch [::search]}))

(re-frame/reg-event-fx
  ::list-patients-success
  (fn [{:keys [db]} [_ result]]
    {:db (-> db
             (assoc :loading false)
             (assoc :patients result))}))

(re-frame/reg-event-fx
  ::list-patients-failure
  (fn [{:keys [db]} [_]]
    {:db (-> db
             (assoc :loading false))}))

(re-frame/reg-sub
  ::patients
  (fn [db]
    (:patients db)))

(re-frame/reg-event-fx
  ::open-edit-patient-form
  (fn [{:keys [_db]} [_ {id :patient-id}]]
    {:dispatch [::set-active-route {:route-name :edit-patient
                                    :params {:patient-id id}}]}))

(re-frame/reg-event-fx
  ::init-edit-patient
  (fn [{:keys [db]} [_ {id :patient-id}]]
    {:db (-> db
             (assoc :form {})
             (assoc :loading true)
             (assoc :patient-id id)
             (dissoc :form-errors))
     :dispatch [::api/load-patient
                {:patient-id id}
                {:on-success [::load-patient-success]
                 :on-failure [::load-patient-failure]}]}))

(re-frame/reg-event-fx
  ::load-patient-success
  (fn [{:keys [db]} [_ result]]
    {:db (-> db
             (assoc :loading false)
             (assoc :form result))}))

(re-frame/reg-event-fx
  ::load-patient-failure
  (fn [{:keys [db]} [_]]
    {:db (-> db
             (assoc :loading false))}))

(re-frame/reg-event-fx
  ::cancel-edit-patient
  (fn [{:keys [_db]} [_]]
    {:dispatch [::set-active-route {:route-name :patients}]}))

(re-frame/reg-event-fx
  ::edit-patient
  (fn [{:keys [db]} [_]]
    (let [patient-id (:patient-id db)
          form-data (:form db)]
      (if (v/valid? ::v/edit-patient form-data)
        {:db (-> db
                 (assoc :loading true)
                 (dissoc :form-errors))
         :dispatch [::api/edit-patient
                    {:patient-id patient-id
                     :data form-data}
                    {:on-success [::edit-patient-success]
                     :on-failure [::edit-patient-failure]}]}
        (let [errors (prepare-errors (v/get-errors ::v/edit-patient form-data))]
          {:db (assoc db :form-errors errors)})))))

(re-frame/reg-event-fx
  ::edit-patient-success
  (fn [{:keys [db]} [_ _result]]
    {:db (-> db
             (assoc :loading false)
             (assoc :form {}))
     :dispatch [::set-active-route {:route-name :patients}]}))

(re-frame/reg-event-fx
  ::edit-patient-failure
  (fn [{:keys [db]} [_ {{errors :errors} :response}]]
    (let [prepared (->> errors
                        (map (juxt #(-> % :field keyword) identity))
                        (into {}))]
      {:db (-> db
               (assoc :loading false)
               (assoc :form-errors prepared))})))

(re-frame/reg-event-fx
  ::show-delete-patient-dialog
  (fn [{:keys [db]} [_ patient]]
    {:db (assoc db :patient-to-delete patient)}))

(re-frame/reg-event-fx
  ::cancel-delete
  (fn [{:keys [db]} [_]]
    {:db (dissoc db :patient-to-delete)}))

(re-frame/reg-event-fx
  ::delete-patient
  (fn [{:keys [db]} [_ {:keys [patient-id]}]]
    {:db (-> db
             (assoc :loading true))
     :dispatch [::api/delete-patient
                {:patient-id patient-id}
                {:on-success [::delete-patient-success]
                 :on-failure [::delete-patient-failure]}]}))

(re-frame/reg-event-fx
  ::delete-patient-success
  (fn [{:keys [db]} [_]]
    {:db (-> db
             (assoc :loading false)
             (dissoc :patient-to-delete))
     :dispatch [::init-list-patients]}))

(re-frame/reg-event-fx
  ::delete-patient-failure
  (fn [{:keys [db]} [_]]
    {:db (-> db
             (assoc :loading false)
             (assoc :delete-error true))}))

(re-frame/reg-sub
  ::patient-to-delete
  (fn [db]
    (:patient-to-delete db)))

(re-frame/reg-sub
  ::search-values
  (fn [db]
    (:search-values db)))

(re-frame/reg-event-fx
  ::set-search-value
  (fn [{:keys [db]} [_ field value]]
    {:db (assoc-in db [:search-values field] value)}))

(re-frame/reg-event-fx
  ::search
  (fn [{:keys [db]} [_]]
    (let [search (as-> db s
                       (:search-values s)
                       (update s :q s/split #" ")
                       (remove #(-> % second empty?) s)
                       (into {} s))
          limits (:limits db)]
      {:db (assoc db :loading true)
       :dispatch [::api/list-patients
                  (merge search limits)
                  {:on-success [::list-patients-success]
                   :on-failure [::list-patients-failure]}]})))

(re-frame/reg-sub
  ::loading
  (fn [db]
    (:loading db)))

(re-frame/reg-event-fx
  ::reset-search
  (fn [{:keys [db]} [_]]
    {:db (assoc db
                :search-values {}
                :reset-search-key (str (rand)))
     :dispatch [::search]}))

(re-frame/reg-sub
  ::reset-search-key
  (fn [db]
    (:reset-search-key db)))

(re-frame/reg-event-fx
  ::set-page
  (fn [{:keys [db]} [_ page]]
    {:db (assoc-in db [:limits :page] page)
     :dispatch [::search]}))

(re-frame/reg-event-fx
  ::set-items-on-page
  (fn [{:keys [db]} [_ page-size]]
    {:db (assoc-in db [:limits :page-size] page-size)
     :dispatch [::search]}))

(comment
  @re-frame.db/app-db

  (re-frame/dispatch [::set-active-route {:route-name :patients}])
  (re-frame/dispatch [::set-active-route {:route-name :create-patient}])
  @(re-frame/subscribe [::form-errors])
  @(re-frame/subscribe [::form-data])


  (re-frame/dispatch [::set-active-route {:route-name :edit-patient
                                          :params {:patient-id 14}}])

  @(re-frame/subscribe [::patients])

  (as-> @(re-frame/subscribe [::search-values]) s
        (update s :q s/split #" ")
        (remove #(-> % second empty?) s)
        (into {} s)))
