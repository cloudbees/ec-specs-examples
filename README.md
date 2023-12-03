# ec-specs Examples
ec-specs is a specification and acceptance testing framework based on [Spock](https://spockframework.org/) and [CloudBees CDRO DSL](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslabout) for testing your CloudBees CDRO releases, pipelines and more. This repository includes example specifications for testing sample CDRO releases, pipelines, DSL scripts, etc and a Maven [pom.xml](pom.xml) for building and running the example specifications. 

## Prerequisites
* JDK 17 or higher
* [Maven version 3.8.6 or higher](https://maven.apache.org/download.cgi)
* CloudBees CDRO 2023.10.0 or higher test environment - The CDRO server does not need to be running locally. It can be running on a remote server or in a kubernetes cluster.

## Building and running tests
### Compile
```
mvn compile
```
`com.electriccloud.commander-spec-tests-core` jar is downloaded from the CloudBees repository at `https://repo.cloudbees.com/content/repositories/artifacts.cloudbees.com_maven-public-releases`. 

### Compile and test
1. Install a test instance of CloudBees CDRO againt which the spec tests will be run.
2. (a) For the CDRO server running on `localhost` with the default password for `admin` user:
```
mvn test
```
2. (b) For the CDRO server running on a remote server, e.g., `test-remote-server`, using user credentials `test-user`/`test-user-password`:
```
mvn -DCOMMANDER_SERVER=test-remote-server -DCOMMANDER_USER=test-user -DCOMMANDER_PASSWORD=test-user-password test
```

## Additional resources
* [CloudBees CDRO DSL](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslabout#_dsl_automation_as_code)
* [Spock Primer](https://spockframework.org/spock/docs/1.0/spock_primer.html)
* [DSL methods reference](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslmethods)