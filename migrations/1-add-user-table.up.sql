CREATE TABLE users (
  username TEXT NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  encrypted_password TEXT NOT NULL,
  email TEXT,

  PRIMARY KEY (username)
);

