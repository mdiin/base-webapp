-- name: user-by-username
-- Retrieves the user with the given username.
SELECT
  name,
  username,
  email,
  photo,
  encrypted_password AS password
FROM users
WHERE username = :username;

-- name: new-user<!
-- Inserts new wor in the users table
INSERT INTO users (name, username, encrypted_password)
VALUES (:name, :username, :encrypted_password);

