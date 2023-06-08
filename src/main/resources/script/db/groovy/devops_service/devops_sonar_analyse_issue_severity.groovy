package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_sonar_analyse_issue_severity.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_sonar_analyse_issue_severity", remarks: '代码扫描问题分级统计表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: "record_id", type: "BIGINT UNSIGNED", remarks: "devops_sonar_analyse_record.id") {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(64)', remarks: '分类') {
                constraints(nullable: false)
            }
            column(name: 'blocker', type: 'BIGINT UNSIGNED', remarks: 'blocker问题数', defaultValue: "0")
            column(name: 'critical', type: 'BIGINT UNSIGNED', remarks: 'critical问题数', defaultValue: "0")
            column(name: 'major', type: 'BIGINT UNSIGNED', remarks: 'major问题数', defaultValue: "0")
            column(name: 'minor', type: 'BIGINT UNSIGNED', remarks: 'minor问题数', defaultValue: "0")
            column(name: 'info', type: 'BIGINT UNSIGNED', remarks: 'info问题数', defaultValue: "0")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")

            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_sonar_analyse_issue_severity', indexName: 'devops_sonar_analyse_issue_severity_n1') {
            column(name: 'record_id')
        }

    }
}
