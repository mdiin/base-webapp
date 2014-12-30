-- name: pictures
-- Retrieves all pictures for a given user
SELECT *
FROM pictures
WHERE username = :username;

