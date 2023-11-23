package com.cloudbees.cdro.spec.examples

import com.electriccloud.spec.SpockTestSupport

class SimpleDslSpec extends SpockTestSupport {

    def "creating an application in an existing project"() {

        given:
        def args = [projectName: 'myproj']
        dsl "project args.projectName", args

        when:
        def result = dsl "application 'myapp', projectName: args.projectName ", args

        then:
        result.application.projectName == args.projectName

        cleanup:

        deleteProjects(args)
    }

}
