# Pokemon description app

## Running the app
Before you do, **please install the latest Docker version**


### Using pre-compiled binaries
Execute the app source root directory:
- `docker build --tag pokemon .`
- `docker run --publish 8080:8080 pokemon`

### Compiling yourself
If you decide to compile yourself, you'd require java 11 installed
Then follow:

- `./gradlew clean copyJarToBin`
- `docker build --tag pokemon .`
- `docker run --publish 8080:8080 pokemon`

**Further improvements**
- further segregate domain and responsibilities, decouple exception handling from pokemon repository
- make contract stronger by using micro types where possible - e.g. pokemon name string should become its own micro type to utilise type checking in compilation time
- make handling timeouts and other remote errors more robust
- cache results in persistence (db, file, etc) as data is static in remote
- introduce more robust and scalable exception handling with clearer system exception to http status mapping