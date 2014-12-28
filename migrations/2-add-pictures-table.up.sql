CREATE TABLE pictures (
  id serial PRIMARY KEY,
  username text NOT NULL REFERENCES users (username),
  created timestamp without time zone NOT NULL
);

CREATE TABLE albums (
  id serial PRIMARY KEY,
  name text NOT NULL,
  username text NOT NULL REFERENCES users (username),

  UNIQUE (name, username)
);

CREATE TABLE albums_pictures (
  album_id integer NOT NULL REFERENCES albums (id),
  picture_id integer NOT NULL REFERENCES pictures (id)
);

CREATE TABLE albums_users (
  album_id integer NOT NULL REFERENCES albums (id),
  username text NOT NULL REFERENCES users (username),
  role text NOT NULL REFERENCES roles (name)
);

