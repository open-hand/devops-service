package script.db.groovy.devops_service


databaseChangeLog(logicalFilePath: 'dba/devops_branch.groovy') {
    changeSet(author: 'Younger', id: '2018-07-01-create-table') {
        createTable(tableName: "devops_branch", remarks: 'git分支') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用Id')
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id')
            column(name: 'issue_id', type: 'BIGINT UNSIGNED', remarks: 'issueId')
            column(name: 'branch_name', type: 'VARCHAR(64)', remarks: '分支名')
            column(name: 'origin_branch', type: 'VARCHAR(64)', remarks: '来源分支')
            column(name: 'last_commit_date', type: 'DATETIME', remarks: '最后提交时间')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'younger', id: '2018-07-04-add-column') {
        addColumn(tableName: 'devops_branch') {
            column(name: 'commit', type: 'VARCHAR(64)', remarks: 'commit', afterColumn: 'origin_branch')
        }
        addUniqueConstraint(tableName: 'devops_branch',
                constraintName: 'uk_branch_name_commit', columnNames: 'branch_name,commit')
    }

    changeSet(author: 'runge', id: '2018-07-10-add-column') {
        addColumn(tableName: 'devops_branch') {
            column(name: 'is_deleted', type: 'TINYINT UNSIGNED', remarks: '分支是否删除', afterColumn: 'commit', defaultValue: "0")
        }
    }

    changeSet(author: 'runge', id: '2018-07-10-rename-column-and-add-last-commit') {
        renameColumn(tableName: 'devops_branch', columnDataType: 'VARCHAR(64)',
                oldColumnName: 'commit', newColumnName: 'checkout_commit', remarks: 'checkout sha')
        renameColumn(tableName: 'devops_branch', columnDataType: 'DATETIME',
                oldColumnName: 'last_commit_date', newColumnName: 'checkout_date', remarks: 'checkout date')
        addColumn(tableName: 'devops_branch') {
            column(name: 'last_commit', type: 'VARCHAR(64)', remarks: '最新提交', afterColumn: 'checkout_date')
            column(name: 'last_commit_msg', type: 'VARCHAR(512)', remarks: '最新提交信息', afterColumn: 'last_commit')
            column(name: 'last_commit_user', type: 'BIGINT UNSIGNED', remarks: '最新提交用户Id', afterColumn: 'last_commit_msg')
            column(name: 'last_commit_date', type: 'DATETIME', remarks: '最新提交时间', afterColumn: 'last_commit_user')
        }
    }

    changeSet(author: 'crockitwood', id: '2018-09-28-drop-branch-constraint') {
        dropUniqueConstraint(constraintName: "uk_branch_name_commit",tableName: "devops_branch")
    }

    changeSet(author: 'younger', id: '2019-05-27-add-index') {
        createIndex(indexName: "idx_branchname_appid_isdeleted_creationdate ", tableName: "devops_branch") {
            column(name: "branch_name")
            column(name: "app_id")
            column(name: "is_deleted")
            column(name: "creation_date")
        }
    }

    changeSet(author: 'scp', id: '2019-06-13-add-index') {
        createIndex(indexName: "idx_app_id", tableName: "devops_branch") {
            column(name: "app_id")
        }
    }


    changeSet(author: 'sheep', id: '2019-06-13-add-column') {
        addColumn(tableName: 'devops_branch') {
            column(name: 'status', type: 'VARCHAR(32)', remarks: '分支创建状态', afterColumn: 'branch_name', defaultValue: "success")
            column(name: 'error_message', type: 'VARCHAR(5000)', remarks: '分支创建失败错误信息', afterColumn: 'status')
        }
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_branch')
    }

    changeSet(author: 'lihao', id: '2020-06-29-devops_branch-add-index') {
        createIndex(indexName: "idx_devops_branch_issueid", tableName: "devops_branch") {
            column(name: "issue_id")
        }
    }
    changeSet(author: 'wanghao', id: '2020-08-09-devops_branch-modify-column') {
        modifyDataType(tableName: 'devops_branch', columnName: 'branch_name', newDataType: 'VARCHAR(512)')
    }
}