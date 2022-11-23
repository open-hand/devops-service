package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_tpl_sonar_quality_gate.groovy') {
    changeSet(author: 'lihao', id: '2022-11-18-create-table') {
        createTable(tableName: "devops_ci_tpl_sonar_quality_gate", remarks: 'ci sonar配置 质量门条件表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: 'devops_ci_sonar_config表的id') {
                constraints(nullable: false)
            }
            column(name: 'gates_enable', type: 'TINYINT(1)', remarks: '是否启用质量门禁', defaultValue: 0)
            column(name: 'gates_block_after_fail', type: 'TINYINT(1)', remarks: '质量门失败后是否阻塞后续job 默认是', defaultValue: 1)
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(tableName: 'devops_ci_tpl_sonar_quality_gate', indexName: 'devops_ci_tpl_sonar_quality_gate_n1') {
            column(name: 'config_id')
        }
    }
}