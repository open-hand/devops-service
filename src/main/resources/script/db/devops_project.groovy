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


    changeSet(author: 'younger', id: '2018-07-25-add-column')
            {
                addColumn(tableName: 'devops_project') {
                    column(name: 'type', type: 'VARCHAR(50)', remarks: 'gitlab project type', afterColumn: 'gitlab_group_id')
                }
            }
}