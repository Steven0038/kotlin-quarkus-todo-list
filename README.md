# todo-list project

this is a simple todolist project, with basic todo item crud apis and GUI

## features
- quarkus based PanacheMongo CRUD with restful api
- simple GUI with qute
- kotlin functional programming with Arrow

## Tech stack
- Kotlin
- Quarkus
- Arrrow
- quarkus qute template for GUI
- Unit test ( not full covered)
  - ktest
  - junit
- Open API ( only for restful apis)
- mongo DB 

## Build and Version
### version
- openJDK 17
- kotlin 1.7.1
- quarkus 2.1.0.Final

### db connecting string
```see application.properties```

### visit the home page
``` localhost:8080/todo ```

### visit the open api page
``` localhost:8080/q/swagger-ui/#/ ```

## References
- [arrow demo by hmchangm](https://github.com/hmchangm/quarkus-reactive-kotlin)
- [qute demo by gunnarmorling](https://github.com/gunnarmorling/quarkus-qute) ( Apache-2.0 license)
  - including /resource/templates*

## Licence
Apache-2.0 license

---
This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory. Be aware that it’s not an _über-jar_ as
the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/todolist-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
