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

class ProcedureRunSpec
    extends SpockTestSupport {

    def "successful procedure run"() {

        def projectName = randomize('Hello Project')

        given: 'a procedure'
        dsl """
			project ('$projectName') {
			  procedure ('testProcedure') {
				formalParameter('friend')  {
				  required=1
				  defaultValue='Kara'
				}
				step ('hello') {
				    command= 'print "Hello \$[friend] from CDRO DSL!";'
				    shell='ec-perl'
				}
			  }
			}
			"""

        when: 'the procedure is run'
        def result = dsl """
				runProcedure(
					projectName: '$projectName',
					procedureName: 'testProcedure',
					actualParameter: [
					  friend: 'Barry',
					]
				)
			"""

        then: 'procedure completes successfully'
        assert result?.jobId
        jobSucceeded result.jobId

        cleanup:
        deleteProjects([projectName: projectName])
    }


}