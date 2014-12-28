CREATE TABLE users (
  username TEXT NOT NULL,
  name TEXT NOT NULL,
  encrypted_password TEXT NOT NULL,
  photo TEXT,
  email TEXT,

  PRIMARY KEY (username)
);

CREATE TABLE roles (
  name text PRIMARY KEY
);

INSERT INTO roles (name) VALUES ('owner');
INSERT INTO roles (name) VALUES ('read');
INSERT INTO roles (name) VALUES ('write');

