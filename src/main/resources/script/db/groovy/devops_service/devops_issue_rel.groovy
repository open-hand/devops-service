package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_issue_rel.groovy') {
    changeSet(author: 'lihao', id: '2021-05-21-create-table') {
        createTable(tableName: "devops_issue_rel", remarks: '敏捷问题与分支或提交关联关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'object', type: 'VARCHAR(32)', remarks: '关联对象 分支或提交') {
                constraints(nullable: false)
            }
            column(name: 'object_id', type: 'BIGINT UNSIGNED', remarks: '关联对象id') {
                constraints(nullable: false)
            }
            column(name: 'issue_id', type: 'BIGINT UNSIGNED', remarks: '敏捷的issueId') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        createIndex(indexName: "idx_object_id_issue_id_object", tableName: "devops_issue_rel") {
            column(name: "object_id")
            column(name: 'issue_id')
            column(name: 'object')
        }
        addUniqueConstraint(tableName: 'devops_issue_rel', constraintName: 'uk_object_id_issue_id_object', columnNames: 'object_id,issue_id,object')
    }

    changeSet(author: 'lihao', id: '2021-06-06-add-column-branch-id') {
        addColumn(tableName: 'devops_issue_rel') {
            column(name: 'branch_id', type: 'BIGINT UNSIGNED', remarks: '该条记录所属branchId', afterColumn: 'issue_id')
        }
    }

    changeSet(author: 'lihao', id: '2021-06-08-add-column-branch-id') {
        addColumn(tableName: 'devops_issue_rel') {
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '该条记录所属项目id', afterColumn: 'branch_id')
            column(name: 'service_code', type: 'VARCHAR(64)', remarks: '该条记录所属应用code', afterColumn: 'project_id')
        }
    }
}