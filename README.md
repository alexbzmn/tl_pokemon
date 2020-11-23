# Pokemon description app

**N.B.**: Many descriptions per pokemon can be retrieved using the api, and the app is picking one at random. 
Normally, I would avoid such an implicit behaviour and double check the requirements, but because I was doing this challenge on Sunday evening, I didn't have such possibility. Please note.

Also, the translation service (free version) has rate limiting restricting only to several calls per hour, please mind when testing.

## Running the app
Before you do, **please install the latest Docker version**


### Running using pre-compiled binaries
Execute from the app source root directory:
- `docker build --tag pokemon .`
- `docker run --publish 5000:5000 pokemon`

### Running from sources (compiling yourself)
**If you decide to compile yourself, you'd require java 11 installed and JAVA_HOME var pointing to the java binaries folder**

Then run the following:

- `cd to the project folder`
- `chmod -R 777 ./`
- `./gradlew clean shadowJar`
- `docker build --tag pokemon .`
- `docker run --publish 5000:5000 pokemon`

### Running tests:
`./gradlew clean test`

## Further improvements
- further segregate domain and responsibilities, decouple exception handling from pokemon repository
- make the business logic contract stronger by using micro types where possible - e.g. pokemon name string should become its own micro type to utilise type checking in compilation time
- cache results (db, file, etc) as data is static in remote
- introduce more robust and scalable exception handling with clearer system exception to http status mapping
- add more sensible validations of the client input
