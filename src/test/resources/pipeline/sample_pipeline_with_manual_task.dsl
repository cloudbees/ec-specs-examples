project args.projectName, {
    description = 'This is a test project'

    pipeline 'test_pipeline', {

      formalParameter 'pipeline_param', {
        orderIndex = 1
        required = true
        type = 'entry'
      }

      stage 'test_stage', {

        task 'test_manual_task', {
          description = 'This is a manual task'
          instruction = 'This is a manual stage task in the project: $[/myProject/name]'
          notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
          skippable = false
          taskType = 'MANUAL'
          approver = [
            'approver_user',
          ]

          formalParameter 'param1', {
            orderIndex = 1
            required = true
            type = 'textarea'
          }

          formalParameter 'param2', {
            orderIndex = 2
            required = true
            type = 'entry'
          }
        }
      }
    }
}