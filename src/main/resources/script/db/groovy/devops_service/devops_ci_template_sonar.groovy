package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_sonar.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table-devops_ci_template_sonar') {
        createTable(tableName: "devops_ci_template_sonar", remarks: 'devops_ci_template_sonar') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'scanner_type', type: 'VARCHAR(255)', remarks: '扫描器类型 sonarmaven 、sonarscanner') {
                constraints(nullable: false)
            }
            column(name: 'ci_template_step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤Id') {
                constraints(nullable: false)
            }
            column(name: 'skipTests', type: 'TINYINT UNSIGNED', remarks: '是否跳过单测')

            column(name: 'sources', type: 'VARCHAR(1024)', remarks: '要扫描的文件目录，多个文件夹使用,隔开')

            column(name: 'sonar_url', type: 'VARCHAR(2000)', remarks: '外部sonar地址')
            column(name: 'auth_type', type: 'VARCHAR(2000)', remarks: '外部sonar认证类型')
            column(name: 'username', type: 'VARCHAR(2000)', remarks: '外部sonar认证的用户名')
            column(name: 'password', type: 'VARCHAR(2000)', remarks: '外部sonar认证的密码')
            column(name: 'token', type: 'VARCHAR(2000)', remarks: '外部sonar认证的token')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_template_sonar', constraintName: 'uk_ci_template_step_id', columnNames: 'ci_template_step_id')
    }

}