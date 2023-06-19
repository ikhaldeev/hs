-- :name insert-patient :<!
-- :doc Insert a single patient
insert into patients (first_name, middle_name, last_name, sex, dob, address, policy_number)
values (:first-name, :middle-name, :last-name, :sex, :dob, :address, :policy-number)
returning id

-- :name patient-by-id :? :1
-- :doc Get patient by id
select * from patients
where id = :id

-- :name list-patients :? :*
-- :doc Get patients list by query params
select * from patients
where true
--~ (when (:q params) ":snip*:q")
