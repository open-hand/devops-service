package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_template_permission.groovy') {
    changeSet(author: 'scp', id: '2021-03-09-create-table') {
        createTable(tableName: "devops_app_template_permission", remarks: '应用模板权限表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_template_id', type: 'BIGINT UNSIGNED', remarks: '应用模板Id')
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: 'iam用户Id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_app_template_permission',
                constraintName: 'uk_template_user_id', columnNames: 'app_template_id,user_id')
    }

}