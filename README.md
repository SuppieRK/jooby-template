# Jooby template
This template project is a Java-based backend service based on [Jooby](https://github.com/jooby-project/jooby) web framework and jOOQ for database interaction.
​
### Setup summary
*   Java 21
*   Jooby as one of the fastest, pure Java web frameworks available
*   Combination of Flyway, jOOQ and codegen Gradle plugin to reduce complexity and allow direct, typesafe operations with database using SQL.
*   Spotless to ensure consistent code format across repository files.
*   OpenAPI specification generation.
*   Barebone metrics support, ready to be plugged in to the metrics backend of your choice.
*   Baseline separation of concerns between controllers in functional style.
​
## Using​
*   [Jooby](https://jooby.io/) - The web framework used
*   [jOOQ](https://www.jooq.org/) - Database-first ORM
*   [Flyway](https://flywaydb.org/) - Database migration tool
*   [jOOQ Java Class Generator Gradle plugin](https://github.com/SuppieRK/jooq-java-class-generator) - SQL code generation for type safety
*   [PostgreSQL](https://www.postgresql.org/) - Database
*   [Gradle](https://gradle.org/) - Dependency Management
