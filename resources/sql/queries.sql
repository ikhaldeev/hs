-- :name insert-patient :<!
-- :doc Insert a single patient
insert into patients (first_name, middle_name, last_name, sex, dob, address, policy_number)
values (:first-name, :middle-name, :last-name, :sex, :dob, :address, :policy-number)
returning id

-- :name update-patient :! :n
-- :doc Update a single patient
update patients
set first_name = :first-name, middle_name = :middle-name, last_name = :last-name,
sex = :sex, dob = :dob, address = :address, policy_number = :policy-number
where id = :id

-- :name delete-patient :! :n
-- :doc Delete a single patient
delete from patients
where id = :id

-- :name patient-by-id :? :1
-- :doc Get patient by id
select * from patients
where id = :id

-- :name list-patients :? :*
-- :doc Get patients list by query params
select * from patients
where true
--~ (when (:q params) ":snip*:q")
--~ (when (:dob-start params) " and dob >= :dob-start")
--~ (when (:dob-end params) " and dob <= :dob-end")
--~ (when (:sexes params) " and sex in (:v*:sexes)")
--~ (when (:policy-number-starts params) " and policy_number ilike :policy-number-starts")

