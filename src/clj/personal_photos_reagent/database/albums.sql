-- name: albums-for-user
-- Retrieves all albums for a given user
SELECT
  a.name AS albumname,
  au.role
FROM albums_users au
LEFT JOIN albums a
ON a.id = au.album_id
WHERE au.username = :username;

-- name: albums-for-picture
-- Retrieves all albums for a given picture
SELECT
  a.name AS albumname
FROM albums_pictures ap
LEFT JOIN albums a
ON a.id = ap.album_id
WHERE ap.picture_id = :picture-id;

