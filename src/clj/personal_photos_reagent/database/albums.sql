-- name: albums
-- Retrieves all albums for a given user
SELECT
  a.name AS albumname,
  au.role
FROM albums_users au
LEFT JOIN albums a
ON a.id = au.album_id
WHERE au.username = :username;

