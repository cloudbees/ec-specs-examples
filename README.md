# ec-specs examples
ec-specs is a specification and acceptance testing framework based on [Spock](https://spockframework.org/) and [CloudBees CD/RO DSL](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslabout). This repository includes example specifications for testing sample CloudBees CD/RO releases, pipelines, DSL scripts, etc. Additionally, a Maven [pom.xml](pom.xml) is included for building and running example specifications. 

## Prerequisites
* JDK 17 or higher
* [Maven version 3.8.6 or higher](https://maven.apache.org/download.cgi)
* CloudBees CD/RO v2023.10.0 or higher test environment 
> **_NOTE:_**  The CloudBees CD/RO server can be running locally, on a remote server, or in a Kubernetes cluster.


## Compile tests
To compile ec-spec, run:
```
mvn compile
```
This command downloads the `com.electriccloud.commander-spec-tests-core.jar` from the [CloudBees repository](https://repo.cloudbees.com/content/repositories/artifacts.cloudbees.com_maven-public-releases).

## Run tests
After you have installed a test instance of CloudBees CD/RO, to run the spec tests:

* For a CloudBees CD/RO server running on a `localhost` with the default password for `admin` user:
  ```
  mvn test
  ```
* For the CloudBees CD/RO server running on a remote server, for example `test-remote-server`, using user credentials `test-user`/`test-user-password`:
  ```
  mvn -DCOMMANDER_SERVER=test-remote-server -DCOMMANDER_USER=test-user -DCOMMANDER_PASSWORD=test-user-password test
  ```

## Write specification (spec test) classes
Spock allows you to write specifications that describe expected features exhibited by a system. An ec-specs specification is a Groovy class with a naming pattern `<Something>Spec` that extends from `com.electriccloud.spec.SpockTestSupport`, which extends from `spock.lang.Specification`.

### Structure of a simple specification class
The following is an example of a simple specification class:

```groovy
import com.electriccloud.spec.SpockTestSupport

class MySpec extends SpockTestSupport {

    def "my dsl works as expected"() {

        given: 'a project'
        // Use a random name for the project to ensure the test is isolated from other feature method runs. 
        def args = [projectName: randomize ('myproj')]
        dsl "project args.projectName", args

        when: 'an application is created using the given dsl'
        def result = dsl "application 'myapp', projectName: args.projectName ", args

        then: 'the application creation is successful'
        result.application.projectName == args.projectName

        cleanup:
        // Clean up after the feature method run.
        deleteProjects(args)
    }

    def "another test"() {
        //...
    }
    
}
```
* `MySpec` contains two feature methods:
  * `my dsl works as expected`
  * `another test`
* Before the feature methods in `MySpec` run, a new session is established for `COMMANDER_USER` on the CloudBees CD/RO test instance  `COMMANDER_SERVER`. 
* The session is invalidated after completion of all the feature methods in `MySpec`.    

For more details on writing specifications, refer to the example specifications and [Spock](https://spockframework.org/).

## Additional resources
* [CloudBees CD/RO DSL](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslabout#_dsl_automation_as_code)
* [Spock Primer](https://spockframework.org/spock/docs/1.0/spock_primer.html)
* [DSL methods reference](https://docs.cloudbees.com/docs/cloudbees-cd-api/latest/flow-api/dslmethods)
