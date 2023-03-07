package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_sonar_quality_gate_condition.groovy') {
    changeSet(author: 'lihao', id: '2022-11-18-create-table') {
        createTable(tableName: "devops_ci_sonar_quality_gate_condition", remarks: 'ci sonar配置 质量门条件表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'sonar_condition_id',type: 'VARCHAR(128)',remarks: '在sonar的id'){
                constraints(nullable: false)
            }
            column(name: 'gate_id', type: 'BIGINT UNSIGNED', remarks: '关联的质量门id') {
                constraints(nullable: false)
            }

            column(name: 'gates_metric',type: 'VARCHAR(128)',remarks: '质量门类型')
            column(name: 'gates_operator',type: 'VARCHAR(128)',remarks: '质量门比较操作')
            column(name: 'gates_value',type: 'VARCHAR(128)',remarks: '质量门值')
            column(name: 'gates_scope',type: 'VARCHAR(16)',remarks: '质量门扫描范围 new 新代码 all 全量代码')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(tableName: 'devops_ci_sonar_quality_gate_condition', indexName: 'devops_ci_sonar_quality_gate_condition_n1') {
            column(name: 'gate_id')
        }
    }
}