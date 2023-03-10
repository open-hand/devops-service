package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_record.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_pipeline_record", remarks: 'ci流水线执行记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab流水线记录id')
            column(name: 'commit_sha', type: 'VARCHAR(255)', remarks: 'commit_sha')
            column(name: 'gitlab_trigger_ref', type: 'VARCHAR(255)', remarks: '触发分支')
            column(name: 'status', type: 'VARCHAR(255)', remarks: '流水线状态')
            column(name: 'trigger_user_id', type: 'BIGINT UNSIGNED', remarks: '触发用户id')
            column(name: "created_date", type: "DATETIME", remarks: 'gitlab流水线创建时间')
            column(name: "finished_date", type: "DATETIME", remarks: 'gitlab流水线结束时间')
            column(name: "duration_seconds", type: "BIGINT UNSIGNED", remarks: '流水线执行时长')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_record',
                constraintName: 'uk_gitlab_pipeline_id', columnNames: 'gitlab_pipeline_id')
    }
    changeSet(author: 'wanghao', id: '2020-04-13-add-column') {
        addColumn(tableName: 'devops_ci_pipeline_record') {
            column(name: "gitlab_project_id", type: "BIGINT UNSIGNED", remarks: 'gitlab_project_id')
        }
        createIndex(tableName: 'devops_ci_pipeline_record', indexName: 'ci_pipeline_record_gpid_idx') {
            column(name: 'gitlab_project_id')
        }
    }

    changeSet(author: 'wanghao', id: '2020-10-19-add-column') {
        dropUniqueConstraint(tableName: 'devops_ci_pipeline_record',
                constraintName: 'uk_gitlab_pipeline_id')

        addUniqueConstraint(tableName: 'devops_ci_pipeline_record',
                constraintName: 'uk_gitlab_devops_pipeline_id', columnNames: 'gitlab_pipeline_id, ci_pipeline_id')
    }

    changeSet(author: 'wanghao', id: '2022-12-13-add-column') {
        addColumn(tableName: 'devops_ci_pipeline_record') {
            column(name: "source", type: "VARCHAR(255)", remarks: 'gitlab source', afterColumn: "gitlab_trigger_ref")
        }
    }
    changeSet(author: 'wanghao', id: '2022-12-19-add-index') {
        createIndex(tableName: 'devops_ci_pipeline_record', indexName: 'devops_ci_pipeline_record_n2') {
            column(name: 'ci_pipeline_id')
        }

    }
    changeSet(author: 'wanghao', id: '2023-2-07-add-column') {
        addColumn(tableName: 'devops_ci_pipeline_record') {
            column(name: "queued_duration", type: "BIGINT UNSIGNED", remarks: '流水线排队时长', afterColumn: "duration_seconds")
        }

    }
}
