# base-webapp

A base for sente+reagent webapps.

## Usage

Clone and extend.

## Running

Use a leiningen version above 2.3.4.

The `dev` profile enables figwheel and the `user` namespace:

1. `$ lein with-profile dev repl`
2. `$ lein with-profile dev figwheel`
3. `> (user/restart)`

A server is now started on `localhost:8091`.

Development expects the database to be at:

- `DB_HOST=localhost`
- `DB_NAME=development`
- `DB_USER=development`
- `DB_PASS=development`

## Troubleshooting

Usually running

`$ lein clean` 

will fix whatever project-wide issues might occur.

## License

Copyright © 2014 Matthias Diehn Ingesman

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
