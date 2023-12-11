/*
*
* Copyright 2023 CloudBees, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.cloudbees.cdro.spec.examples

import com.electriccloud.spec.SpockTestSupport

class SimpleDslSpec extends SpockTestSupport {

    def "creating an application in an existing project"() {

        given: 'a project'
        // Use a random name for the project to ensure the test is isolated from other feature method runs.
        def args = [projectName: randomize ('myproj')]
        dsl "project args.projectName", args

        when: 'an application is created'
        def result = dsl "createApplication applicationName: 'myapp', projectName: args.projectName ", args

        then: 'the application creation is successful'
        result.application.projectName == args.projectName

        cleanup:
        // Cleanup the content created in the feature method at the end of the run
        deleteProjects(args)
    }

}
