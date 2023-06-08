package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_sonar_analyse_measure.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_sonar_analyse_measure", remarks: '代码扫描指标详情表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: "record_id", type: "BIGINT UNSIGNED", remarks: "devops_sonar_analyse_record.id") {
                constraints(nullable: false)
            }
            column(name: 'metric', type: 'VARCHAR(64)', remarks: '键')
            column(name: 'value', type: 'VARCHAR(4000)', remarks: '值')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")

            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_sonar_analyse_measure', indexName: 'devops_sonar_analyse_measure_n1') {
            column(name: 'record_id')
        }

    }
}
