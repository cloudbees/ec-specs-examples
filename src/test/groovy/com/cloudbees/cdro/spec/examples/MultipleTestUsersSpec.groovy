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

class MultipleTestUsersSpec
    extends SpockTestSupport {

    def "test user access" (def user1, def user2, def password, def projectName) {

        given: "a project with only one user with access to the project"
        dsl """
        user('$user1', password: '$password')
        user('$user2', password: '$password')
        project '$projectName', {
          acl {
            // break inheritance so that only user1 should have access which will be granted below
            inheriting = '0'
        
            aclEntry 'user', principalName: '$user1', {
              changePermissionsPrivilege = 'allow'
              executePrivilege = 'allow'
              modifyPrivilege = 'allow'
              readPrivilege = 'allow'
            }
          }
        }
        """

        when: "logged in as $user1"
        login(user1, password)

        then: "the project can be modified"
        def result = dsl "modifyProject projectName: '$projectName', description: 'desc'"
        assert result?.project?.projectName == projectName
        assert result?.project?.description == 'desc'
        assert result?.project?.lastModifiedBy == user1

        when: "logged in as $user2"
        login(user2, password)

        then: "the project can not be accessed"
        // passing ignoreStatusCode:true in the options to the dsl helper since we do expect this call to fail
        def result2 = dsl "getProject projectName: '$projectName'", null, [ignoreStatusCode:true]
        result2?.error?.message =~ / AccessDenied: Principal '$user2' does not have read privilege on project '$projectName'/

        cleanup:
        // switch to default login in order to cleanup the test data
        assertLogin(config.userName, config.password)
        deleteUser user1
        deleteUser user2
        deleteProjects ([project: projectName])

        where:
        user1               | user2               | password          | projectName
        randomize('user11') | randomize('user21') | randomize('pwd1') | randomize('proj1')
        randomize('user12') | randomize('user22') | randomize('pwd2') | randomize('proj2')
        randomize('user13') | randomize('user23') | randomize('pwd3') | randomize('proj3')
        randomize('user14') | randomize('user24') | randomize('pwd4') | randomize('proj4')
    }
}