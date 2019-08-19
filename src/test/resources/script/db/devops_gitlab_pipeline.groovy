package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_gitlab_pipeline.groovyovy') {
    changeSet(author: 'Younger', id: '2018-09-19-create-table') {
        createTable(tableName: "devops_gitlab_pipeline", remarks: 'pipeline表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'pipeline Id') {
                constraints(unique: true)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id')
            column(name: 'pipeline_create_user_id', type: 'BIGINT UNSIGNED', remarks: '触发pipeline用户id')
            column(name: 'commit_id', type: 'BIGINT UNSIGNED', remarks: 'commit id')

            column(name: 'status', type: 'VARCHAR(32)', remarks: 'pipeline状态')
            column(name: 'stage', type: 'VARCHAR(2000)', remarks: 'pipeline阶段信息')
            column(name: 'pipeline_creation_date', type: 'DATETIME', remarks: 'pipeline开始时间')


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'scp', id: '2019-04-16-gitlab-pipeline-add-index') {
        createIndex(indexName: "idx_app_id", tableName: "devops_gitlab_pipeline") {
            column(name: "app_id")
        }
        createIndex(indexName: "idx_pipelineid_commitid", tableName: "devops_gitlab_pipeline") {
            column(name: "pipeline_id")
            column(name: "commit_id")
        }
    }

    changeSet(author: 'younger', id: '2019-05-27-add-index') {
        createIndex(indexName: "idx_commitid ", tableName: "devops_gitlab_pipeline") {
            column(name: "commit_id")
        }
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_gitlab_pipeline')
    }
}