package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_project.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_project", remarks: 'DevOps 项目表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID') {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_group_id', type: 'BIGINT UNSIGNED', remarks: 'GitLab 组 ID') {
                constraints(unique: true, uniqueConstraintName: 'uk_gitlab_group_id')
            }

            column(name: 'gitlab_uuid', type: 'VARCHAR(50)')
            column(name: 'harbor_uuid', type: 'VARCHAR(50)')
            column(name: 'member_uuid', type: 'VARCHAR(50)')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'younger', id: '2018-07-25-add-column') {
        addColumn(tableName: 'devops_project') {
            column(name: 'env_group_id', type: 'BIGINT UNSIGNED', remarks: 'env gitlab group id', afterColumn: 'gitlab_group_id')
        }
    }

    changeSet(author: 'runge', id: '2018-07-30-drop-column') {
        dropColumn(columnName: 'gitlab_uuid', tableName: 'devops_project')
        dropColumn(columnName: 'harbor_uuid', tableName: 'devops_project')
        dropColumn(columnName: 'member_uuid', tableName: 'devops_project')
    }

    changeSet(id: '2018-10-08-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'iam_project_id', oldColumnName: 'id', remarks: 'iam project id', tableName: 'devops_project')
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'devops_app_group_id', oldColumnName: 'gitlab_group_id', remarks: 'gitlab app group id', tableName: 'devops_project')
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'devops_env_group_id', oldColumnName: 'env_group_id', remarks: 'gitlab env group id', tableName: 'devops_project')
    }

    changeSet(author: 'younger', id: '2019-04-30-add-column') {
        addColumn(tableName: 'devops_project') {
            column(name: 'harbor_project_user_name', type: 'VARCHAR(50)', afterColumn: 'devops_env_group_id',remarks: 'harbor项目默认用户')
            column(name: 'harbor_project_user_password', type: 'VARCHAR(50)',afterColumn: 'harbor_project_user_name', remarks: 'harbor项目默认用户密码')
            column(name: 'harbor_project_user_email', type: 'VARCHAR(50)', afterColumn: 'harbor_project_user_password',remarks: 'harbor项目默认用户邮箱')
            column(name: 'harbor_project_is_private', type: 'TINYINT UNSIGNED', defaultValue: "0", afterColumn: 'harbor_project_user_email',remarks: 'harbor仓库是否私有。0公有，1私有')
        }
    }
}