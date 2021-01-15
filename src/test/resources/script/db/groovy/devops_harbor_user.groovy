package script.db.groovy

databaseChangeLog(logicalFilePath: 'devops_harbor_user.groovy') {
    changeSet(id: '2019-10-25-add-table-devops_harbor_user', author: 'lizhaozhong') {
        createTable(tableName: "devops_harbor_user") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'is_push', type: 'TINYINT UNSIGNED', autoIncrement: false, remarks: '是否有push权限')
            column(name: 'harbor_project_user_name', type: 'VARCHAR(50)', autoIncrement: false, remarks: '项目下harbor用户的名称')
            column(name: 'harbor_project_user_password', type: 'VARCHAR(50)', autoIncrement: false, remarks: '项目下harbor用户的密码')
            column(name: 'harbor_project_user_email', type: 'VARCHAR(50)', autoIncrement: false, remarks: '项目下harbor用户的邮箱')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
        changeSet(id: '2019-10-25-add-table-devops_harbor_user', author: 'scp') {
            addUniqueConstraint(tableName: 'devops_harbor_user',
                    constraintName: 'uk_user_name', columnNames: 'harbor_project_user_name')
    }

}