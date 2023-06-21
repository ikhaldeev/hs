(ns hs.views
  (:require
    [re-frame.core :as re-frame]
    [hs.state :as state]))

(defn- full-name
  [{:keys [first-name middle-name last-name]}]
  (str first-name " " (when middle-name (str middle-name " ")) last-name))

(defn- delete-patient-dialog
  []
  (let [patient-to-delete @(re-frame/subscribe [::state/patient-to-delete])]
    (when patient-to-delete
      [:div.dialog-wrapper
       [:div.dialog
        [:h3 "Delete Patient"]
        [:div (str "Are you sure you want to delete " (full-name patient-to-delete) "?")]
        [:div.dialog-controls
         [:button {:on-click #(re-frame/dispatch [::state/cancel-delete])}
          "Cancel"]
         [:button {:on-click #(re-frame/dispatch [::state/delete-patient {:patient-id (:id patient-to-delete)}])}
          "Delete"]]]])))

(defn- patient-item
  [patient]
  [:div {:class-name "patients-item"
         :on-click #(re-frame/dispatch [::state/open-edit-patient-form {:patient-id (:id patient)}])}
   [:div.full-name (full-name patient)]
   [:div.sex (:sex patient)]
   [:div.dob (:dob patient)]
   [:div.address (:address patient)]
   [:div.policy-number (:policy-number patient)]
   [:div.actions {:on-click #(.stopPropagation %)}
    [:button {:on-click #(re-frame/dispatch [::state/show-delete-patient-dialog patient])}
     "Delete"]]])

(defn- patients
  []
  (re-frame/dispatch [::state/init-list-patients])
  (fn []
    (let [{:keys [patients _pages]} @(re-frame/subscribe [::state/patients])]
      [:div.patients-page
       [:h1 "Patients"]
       [:div.patients-items
        (for [{:keys [id] :as patient} patients]
          ^{:key id}
          [patient-item patient])]
       [:div [:button {:on-click #(re-frame/dispatch [::state/open-create-patient-form])}
              "Create patient"]]
       [delete-patient-dialog]])))

(defn- patient-form
  []
  (let [form-data @(re-frame/subscribe [::state/form-data])
        errors @(re-frame/subscribe [::state/form-errors])]
    [:div.form-fields
     [:div.form-field
      [:label "First Name"]
      [:input {:value (:first-name form-data)
               :on-change #(re-frame/dispatch [::state/set-form-value :first-name (.. % -target -value)])}]
      (when (:first-name errors)
        [:label.error (str "Error: " (-> errors :first-name :via))])]
     [:div.form-field
      [:label "Middle Name"]
      [:input {:value (:middle-name form-data)
               :on-change #(re-frame/dispatch [::state/set-form-value :middle-name (.. % -target -value)])}]
      (when (:middle-name errors)
        [:label.error (str "Error: " (-> errors :middle-name :via))])]
     [:div.form-field
      [:label "Last Name"]
      [:input {:value (:last-name form-data)
               :on-change #(re-frame/dispatch [::state/set-form-value :last-name (.. % -target -value)])}]
      (when (:last-name errors)
        [:label.error (str "Error: " (-> errors :last-name :via))])]
     [:div.form-field
      [:label "Sex"]
      [:select {:value (:sex form-data)
                :on-change #(re-frame/dispatch [::state/set-form-value :sex (.. % -target -value)])}
       [:option {:value nil} ""]
       [:option {:value "male"} "Male"]
       [:option {:value "female"} "Female"]
       [:option {:value "other"} "Other"]]
      (when (:sex errors)
        [:label.error (str "Error: " (-> errors :sex :via))])]
     [:div.form-field
      [:label "Date of Birth"]
      [:input {:type "date"
               :value (:dob form-data)
               :on-change #(re-frame/dispatch [::state/set-form-value :dob (.. % -target -value)])}]
      (when (:dob errors)
        [:label.error (str "Error: " (-> errors :dob :via))])]
     [:div.form-field
      [:label "Address"]
      [:textarea {:value (:address form-data)
                  :on-change #(re-frame/dispatch [::state/set-form-value :address (.. % -target -value)])}]
      (when (:address errors)
        [:label.error (str "Error: " (-> errors :address :via))])]
     [:div.form-field
      [:label "Policy Number"]
      [:input {:value (:policy-number form-data)
               :on-change #(re-frame/dispatch [::state/set-form-value :policy-number (.. % -target -value)])}]
      (when (:policy-number errors)
        [:label.error (str "Error: " (-> errors :policy-number :via))])]]))

(defn- create-patient
  []
  (re-frame/dispatch [::state/init-create-patient-form])
  (fn []
    [:div.create-patient-page
     [:h2 "Create Patient"]
     [patient-form]
     [:div.page-actions
      [:div
       [:button {:on-click #(re-frame/dispatch [::state/cancel-create-patient])}
        "Cancel"]]
      [:div
       [:button {:on-click #(re-frame/dispatch [::state/create-patient])}
        "Create"]]]]))

(defn- edit-patient
  [params]
  (re-frame/dispatch [::state/init-edit-patient params])
  (fn [_params]
    [:div.edit-patient-page
     [:h2 "Edit Patient"]
     [patient-form]
     [:div.page-actions
      [:div
       [:button {:on-click #(re-frame/dispatch [::state/cancel-edit-patient])}
        "Cancel"]]
      [:div
       [:button {:on-click #(re-frame/dispatch [::state/edit-patient])}
        "Save"]]]]))

(defn main-panel
  []
  (let [{:keys [route-name params]} @(re-frame/subscribe [::state/active-route])]
    (case route-name
      :create-patient [create-patient params]
      :edit-patient [edit-patient params]
      [patients params])))
