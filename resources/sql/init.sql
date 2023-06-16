create table patients (
  id            serial PRIMARY KEY,
  first_name    text,
  middle_name   text,
  last_name     text,
  sex           text,
  dob           date,
  address       text,
  policy_number text
);
