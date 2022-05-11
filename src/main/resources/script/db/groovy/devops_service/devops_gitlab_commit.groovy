package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'db/devops_gitlab_commit.groovy') {
    changeSet(author: 'n1ck', id: '2018-09-18-create-table') {
        createTable(tableName: "devops_gitlab_commit", remarks: 'gitlab提交记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id')
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户id')
            column(name: 'commit_sha', type: 'VARCHAR(128)', remarks: 'commit sha') {
                constraints(unique: true)
            }
            column(name: 'commit_content', type: 'VARCHAR(2000)', remarks: '提交内容')
            column(name: 'ref', type: 'VARCHAR(128)', remarks: '分支')
            column(name: 'commit_date', type: 'DATETIME', remarks: '提交时间')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'n1ck', id: '2018-09-25-add-column') {
        addColumn(tableName: 'devops_gitlab_commit') {
            column(name: 'app_name', type: 'VARCHAR(128)', remarks: '应用名称', afterColumn: 'commit_date')
        }
    }
    changeSet(author: 'n1ck', id: '2018-09-25-add-column-url') {
        addColumn(tableName: 'devops_gitlab_commit') {
            column(name: 'url', type: 'VARCHAR(512)', remarks: 'commit url', afterColumn: 'app_name')
        }
    }

    changeSet(id: '2018-10-25-modify-constraint', author: 'younger') {
        dropUniqueConstraint(tableName: 'devops_gitlab_commit',
                constraintName: 'commit_sha')
        addUniqueConstraint(tableName: 'devops_gitlab_commit',
                constraintName: 'uk_commit_sha_ref', columnNames: 'commit_sha,ref')
    }

    changeSet(author: 'n1ck', id: '2018-11-06-drop-column') {
        dropColumn(columnName: "app_name", tableName: "devops_gitlab_commit")
    }


    changeSet(author: 'younger', id: '2019-05-27-add-index') {
        createIndex(indexName: "idx_appid_commitdate ", tableName: "devops_gitlab_commit") {
            column(name: "app_id")
            column(name: "commit_date")
        }
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_gitlab_commit')
    }

    changeSet(author: 'wanghao', id: '2020-07-05-add-index') {
        createIndex(indexName: "idx_userid_appid_commitdate ", tableName: "devops_gitlab_commit") {
            column(name: "user_id")
            column(name: "app_service_id")
            column(name: "commit_date")
        }
    }

    changeSet(author: 'zmf', id: '2021-04-13-add-column-issue-id') {
        addColumn(tableName: 'devops_gitlab_commit') {
            column(name: 'issue_id', type: 'BIGINT UNSIGNED', remarks: '敏捷的issueId', afterColumn: 'user_id')
        }
    }
}