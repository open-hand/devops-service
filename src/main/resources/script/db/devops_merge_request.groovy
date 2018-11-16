package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_merge_request.groovy') {
    changeSet(author: 'Runge', id: '2018-04-09-create-table') {
        createTable(tableName: "devops_merge_request", remarks: '应用关联的合并请求') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'application_id', type: 'BIGINT UNSIGNED', remarks: '服务Id')
            column(name: 'source_branch', type: 'VARCHAR(255)', remarks: '源分支')
            column(name: 'target_branch', type: 'VARCHAR(255)', remarks: '目标分支')
            column(name: 'merge_request_id', type: 'BIGINT UNSIGNED', remarks: '合并请求id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_app_source_target_branch", tableName: "devops_merge_request") {
            column(name: "application_id")
            column(name: "source_branch")
            column(name: "target_branch")
        }
    }
    changeSet(author: 'scp', id: '2018-07-10-drop-create-table') {
        dropTable(cascadeConstraints: true, catalogName: "devops_service", schemaName: "public", tableName: "devops_merge_request")
        createTable(tableName: "devops_merge_request", remarks: '应用关联的合并请求') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab项目Id')
            column(name: 'gitlab_merge_request_id', type: 'BIGINT UNSIGNED', remarks: 'merger_request_Id')
            column(name: 'source_branch', type: 'VARCHAR(255)', remarks: '源分支')
            column(name: 'target_branch', type: 'VARCHAR(255)', remarks: '目标分支')
            column(name: 'author_id', type: 'BIGINT UNSIGNED', remarks: '创建者id')
            column(name: "assignee_id", type: "BIGINT UNSIGNED", remarks: '审查人Id')
            column(name: 'state', type: 'VARCHAR(20)', remarks: '状态')
            column(name: 'title', type: 'VARCHAR(255)', remarks: '标题')
            column(name: "created_at", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "updated_at", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        createIndex(indexName: "idx_app_source_target_branch", tableName: "devops_merge_request") {
            column(name: "project_id")
            column(name: "source_branch")
            column(name: "target_branch")
        }
    }
    changeSet(author: 'scp', id: '2018-07-11-create-unique') {
        createIndex(indexName: "idx_project_request", tableName: "devops_merge_request","unique": true) {
            column(name: "project_id",type: 'BIGINT UNSIGNED')
            column(name: "gitlab_merge_request_id",type: 'BIGINT UNSIGNED')
        }
    }

    changeSet(author: 'younger', id: '2018-09-27-add-column') {
                addColumn(tableName: 'devops_merge_request') {
                    column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
                    column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
                    column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
                    column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
                    column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
                }
            }

}