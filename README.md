## gradle-s3-repos
A sample project to publish and resolve artifacts to/from AWS S3 via Gradle's maven repository mechanisms.
- The development branch can be found [here](https://github.com/adrianbk/gradle/tree/s3-maven-publish)

- This is a WIP and has not yet been committed to gradle.

### Steps to publish and resolve

- Publish:

```/gradledev/bin/gradle publish -i```

- Resolve the published dependencies

```/gradledev/bin/gradle compileJava```

  - __Note__ Gradle will cache the artifacts generated during execution of the publish task, manually delete the artifacts from the cache to
  force the dependencies to be downloaded from S3.
