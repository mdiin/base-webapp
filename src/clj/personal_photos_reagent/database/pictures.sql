-- name: pictures
-- Retrieves all pictures for a given user
SELECT *
FROM pictures
WHERE username = :username;

-- name: new-picture<!
-- Insert new picture
INSERT INTO pictures
  (username, created)
VALUES
  (:username, :created);

