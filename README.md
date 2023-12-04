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
1. Install a test instance of CloudBees CDRO against which the spec tests will be run.
2. (a) For the CDRO server running on `localhost` with the default password for `admin` user:
```
mvn test
```
2. (b) For the CDRO server running on a remote server, e.g., `test-remote-server`, using user credentials `test-user`/`test-user-password`:
```
mvn -DCOMMANDER_SERVER=test-remote-server -DCOMMANDER_USER=test-user -DCOMMANDER_PASSWORD=test-user-password test
```

## A specification (spec test) class
Spock allows you to write specifications that describe expected features exhibited by a system of interest. An ec-specs specification is a Groovy class with a naming pattern `<Something>Spec` that extends from `com.electriccloud.spec.SpockTestSupport` that in turn extends from `spock.lang.Specification`.

### Structure of a simple specification class
```groovy
import com.electriccloud.spec.SpockTestSupport

class MySpec extends SpockTestSupport {

    def "my dsl works as expected"() {

        given: 'a project'
        //Use a random name for the project to ensure that the test is isolated from other feature method runs 
        def args = [projectName: randomize ('myproj')]
        dsl "project args.projectName", args

        when: 'an application is created using the given dsl'
        def result = dsl "application 'myapp', projectName: args.projectName ", args

        then: 'the application creation is successful'
        result.application.projectName == args.projectName

        cleanup:
        //clean up after the feature method run
        deleteProjects(args)
    }

    def "another test"() {
        //...
    }
    
}
```
* `MySpec` contains 2 feature methods `my dsl works as expected` and `another test`.
* A new session is established for `COMMANDER_USER` on the test instance of CloudBees CDRO `COMMANDER_SERVER` before the feature methods in `MySpec` run. The session is invalidated after the completion of all the feature methods in `MySpec`.    

Refer to the example specifications and [Spock](https://spockframework.org/) for more details on writing specifications.

## Additional resources
* [CloudBees CDRO DSL](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslabout#_dsl_automation_as_code)
* [Spock Primer](https://spockframework.org/spock/docs/1.0/spock_primer.html)
* [DSL methods reference](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslmethods)