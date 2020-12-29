package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_user.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_user", remarks: 'DevOps 用户表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，Iam用户ID') {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_user_id', type: 'BIGINT UNSIGNED', remarks: 'Gitlab用户ID') {
                constraints(unique: true)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


    changeSet(id: '2018-10-08-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'iam_user_id', oldColumnName: 'id', remarks: 'iam user id', tableName: 'devops_user')
    }

    changeSet(author: 'younger', id: '2018-11-26-add-column') {
        addColumn(tableName: 'devops_user') {
            column(name: 'gitlab_token', type: 'VARCHAR(64)', remarks: 'impersonationToken', afterColumn: 'gitlab_user_id')
        }
    }

    changeSet(author: 'younger', id: '2018-12-05-add-column') {
        addColumn(tableName: 'devops_user') {
            column(name: 'gitlab_user_name', type: 'VARCHAR(64)', remarks: 'gitlab user name', beforeColumn: 'gitlab_token')
        }
    }

    changeSet(author: 'zmf', id: '2019-12-27-add-is-gitlab-admin') {
        addColumn(tableName: 'devops_user') {
            column(name: 'is_gitlab_admin', type: 'TINYINT UNSIGNED', remarks: '是否是gitlab的admin', defaultValue: '0', afterColumn: 'gitlab_user_id')
        }
    }

    changeSet(author: 'zmf', id: '2020-05-14-add-index-gitlab-username') {
        createIndex(indexName: "idx_devops_user_gitlab_user_name ", tableName: "devops_user") {
            column(name: "gitlab_user_name")
        }
    }
}