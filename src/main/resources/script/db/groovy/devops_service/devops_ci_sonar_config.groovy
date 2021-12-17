package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_sonar_config.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table') {
        createTable(tableName: "devops_ci_sonar_config", remarks: 'devops_ci_sonar_config') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤id') {
                constraints(nullable: false)
            }
            column(name: 'scanner_type', type: 'VARCHAR(255)', remarks: '扫描器类型 sonarmaven 、sonarscanner') {
                constraints(nullable: false)
            }
            column(name: 'config_type', type: 'VARCHAR(255)', remarks: '配置类型, 如果是default就不需要其他字段 / default或custom') {
                constraints(nullable: false)
            }
            column(name: 'skip_tests', type: 'TINYINT UNSIGNED', remarks: '是否跳过单测')

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
        addUniqueConstraint(tableName: 'devops_ci_sonar_config',
                constraintName: 'uk_step_id', columnNames: 'step_id')
    }

}