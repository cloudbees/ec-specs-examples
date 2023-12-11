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

class PipelineRunSpec
    extends SpockTestSupport {

    def "complete pipeline with manual task"() {

        def projectName = randomize('test_project')

        given: "an approver user and a non-approver user"
        dsl('''
               user 'approver_user', {
                    email = 'xyz@electric-cloud.com'
                    fullUserName = 'guest'
                    password = 'changeme1'
                    sessionPassword = args.sessionPassword
               }

               aclEntry(principalType: 'user', principalName: 'approver_user',
                    readPrivilege: 'allow', modifyPrivilege: 'deny', executePrivilege: 'allow',
                    objectType: 'server', systemObjectName: 'server')
                    
               user 'non_approver_user', {
                    email = 'xyz@electric-cloud.com'
                    fullUserName = 'test_user'
                    password = 'changeme2'
                    sessionPassword = args.sessionPassword
               }
               
               aclEntry(principalType: 'user', principalName: 'non_approver_user',
                    readPrivilege: 'allow', modifyPrivilege: 'deny', executePrivilege: 'allow',
                    objectType: 'server', systemObjectName: 'server')
                    
            ''', [sessionPassword: config.password])

        and: "a pipeline with manual task with approver_user as approver"
        dslFile ('pipeline/sample_pipeline_with_manual_task.dsl', [projectName: projectName])

        when: 'pipeline is started'
        def flowRTResponse = dsl """runPipeline projectName: '$projectName', pipelineName: 'test_pipeline', 
                                            actualParameter: [pipeline_param: 'pipeline actual param value']"""

        then: 'pipeline run is started and should await manual approval to proceed'
        assert flowRTResponse?.flowRuntime?.flowRuntimeId
        def flowRuntime = flowRTResponse?.flowRuntime
        def flowRuntimeId = flowRuntime?.flowRuntimeId

        // use waitUntil to poll till the pipeline run/flow runtime is awaiting manual approval to proceed
        waitUntil {
            flowRuntime = getPipelineRuntime(flowRuntimeId)
            assert flowRuntime?.hasManualApproval == '1'
            assert flowRuntime?.allowCurrentUserToApprove == '1'
            assert flowRuntime?.stageCount == '1'
            assert flowRuntime?.currentStage == 'test_stage'
        }

        and: 'the task should be awaiting approval'
        waitUntil{
            def task = getPipelineStageRuntimeTask(flowRuntimeId, 'test_stage', 'test_manual_task')
            assert task?.taskType == 'MANUAL'
            assert task?.progressPercentage == '2'
            assert task?.status == 'pending' //pending for approval
            assert task?.instruction == "This is a manual stage task in the project: $projectName" //$[/myProject/name] param in instruction is expanded
            assert task?.taskSkippable == '0'
            assert task?.approvers?.users?.user?.find {it == 'approver_user'}
            assert task?.allowCurrentUserToApprove == '1'
            assert task?.waitingOnManual == '1'
        }

        when: "manual approval is given by user who is not allowed to approve"
        assertLogin('non_approver_user', 'changeme2')
        def result = dsl """ completeManualTask flowRuntimeId: args.flowRuntimeId, 
                                            stageName: 'test_stage', taskName: 'test_manual_task', action: 'completed',
                                            actualParameter: [ param1: "manual task actual parameter1 value", 
                                                               param2: "manual task actual parameter2 value"]   
                    """, [flowRuntimeId: flowRuntimeId], [ignoreStatusCode:true]

        then: "the approval request fails"
        result?.error?.code == 'InvalidScript'
        result?.error?.details =~ /AccessDenied: Principal 'non_approver_user' does not have execute privilege on flowRuntimeState/

        when: "manual approval is given by user who is allowed to approve"
        assertLogin('approver_user', 'changeme1')
        dsl """ completeManualTask flowRuntimeId: args.flowRuntimeId, 
                                            stageName: 'test_stage', taskName: 'test_manual_task', action: 'completed',
                                            actualParameter: [ param1: "manual task actual parameter1 value", 
                                                               param2: "manual task actual parameter2 value"]   
                    """, [flowRuntimeId: flowRuntimeId]

        then: 'manual task is completed successfully'
        assertAdminLogin()
        waitUntil{
            def task = getPipelineStageRuntimeTask(flowRuntimeId, 'test_stage', 'test_manual_task')

            assert task?.taskType == 'MANUAL'
            assert task?.progressPercentage == '100'
            assert task?.status == 'success'
            assert task?.waitingOnManual == '0'
        }

        cleanup:
        // switch to default login in order to cleanup the test data
        assertLogin(config.userName, config.password)
        deleteProjects([projectName: projectName])
        deleteUser 'approver_user'
        deleteUser 'non_approver_user'
    }

    def getPipelineRuntime(def flowRuntimeId){
        def result = dsl "getPipelineRuntimes flowRuntimeId: '$flowRuntimeId'"
        assert result?.flowRuntime?.size()
        return result?.flowRuntime[0]
    }

    def getPipelineStageRuntimeTask(def flowRuntimeId, def stageName, def taskName) {
        def flowRTDetailsResponse = dsl "getPipelineStageRuntimeTasks flowRuntimeId: '$flowRuntimeId', stageName: '$stageName'"
        assert flowRTDetailsResponse?.task
        flowRTDetailsResponse.task.find {it.taskName == taskName}
    }

}