package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_task_record.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_task_record", remarks: '任务记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'stage_record_id', type: 'BIGINT UNSIGNED', remarks: '阶段记录Id')
            column(name: 'task_id', type: 'BIGINT UNSIGNED', remarks: '任务Id')
            column(name: 'task_type', type: 'VARCHAR(20)', remarks: '任务类型')
            column(name: 'status', type: 'VARCHAR(10)', remarks: '状态')
            column(name: 'trigger_version', type: 'VARCHAR(255)', remarks: '触发版本')
            column(name: 'application_id', type: 'BIGINT UNSIGNED', remarks: '应用Id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id')
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例Id')
            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '版本Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'app_deploy_id', type: 'BIGINT UNSIGNED', remarks: '应用部署Id')
            column(name: 'execution_time', type: "DATETIME", remarks: '执行时间')
            column(name: 'is_countersigned', type: 'TINYINT UNSIGNED', remarks: '是否会签')
            column(name: 'value', type: 'TEXT', remarks: '配置信息')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'scp', id: '2019-04-16-devops_pipeline_task-add-column') {
        addColumn(tableName: 'devops_pipeline_task_record') {
            column(name: 'name', type: 'VARCHAR(50)', remarks: '任务名称', afterColumn: "project_id")
        }
    }
    changeSet(author: 'scp', id: '2019-04-18-devops_pipeline_task-drop-column') {
        dropColumn(columnName: "execution_time", tableName: "devops_pipeline_task_record")
    }
    changeSet(author: 'scp', id: '2019-04-25-devops_pipeline_task-add-column') {
        addColumn(tableName: 'devops_pipeline_task_record') {
            column(name: 'instanceName', type: 'VARCHAR(100)', remarks: '实例名称', afterColumn: "project_id")
        }
    }
    changeSet(author: 'scp', id: '2019-04-25-devops_pipeline_task-rename-column') {
        renameColumn(tableName: 'devops_pipeline_task_record', columnDataType: 'VARCHAR(100)',
                oldColumnName: 'instanceName', newColumnName: 'instance_name', remarks: '实例名称')
    }
    changeSet(author: 'scp', id: '2019-04-30-devops_pipeline_task-modify-column') {
        modifyDataType(tableName: 'devops_pipeline_task_record', columnName: 'status', newDataType: 'VARCHAR(50)')
    }
    changeSet(author: 'scp', id: '2019-05-20-devops_pipeline_task-add-column') {
        addColumn(tableName: 'devops_pipeline_task_record') {
            column(name: 'audit_user', type: 'VARCHAR(255)', remarks: '审核人员', afterColumn: "app_deploy_id")
        }
    }
    changeSet(author: 'scp', id: '2019-06-10-devops_pipeline_task-add-column') {
        addColumn(tableName: 'devops_pipeline_task_record') {
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: 'value Id', afterColumn: "app_deploy_id")
        }
    }
    changeSet(author: 'scp', id: '2019-06-12-idx-stage-record-id') {
        createIndex(indexName: "idx_stage_record_id ", tableName: "devops_pipeline_task_record") {
            column(name: "stage_record_id")
        }
    }

    changeSet(author: 'sheep', id: '2019-08-05-devops_pipeline_task-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'application_id', tableName: 'devops_pipeline_task_record')
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_deploy_id', oldColumnName: 'app_deploy_id', tableName: 'devops_pipeline_task_record')
    }
    changeSet(author: 'wanghao', id: '2021-12-11-delete-table') {
        dropTable(tableName: "devops_pipeline_task_record")
    }

}