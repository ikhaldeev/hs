-- :name insert-patient :<!
-- :doc Insert a single patient
insert into patients (first_name, middle_name, last_name, sex, dob, address, policy_number)
values (:first_name, :middle_name, :last_name, :sex, :dob, :address, :policy_number)
returning id

-- :name patient-by-id :? :1
-- :doc Get patient by id
select * from patients
where id = :id
