package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_vuln_scan_target.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_vuln_scan_target", remarks: '漏洞扫描对象记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'scan_record_id', type: 'BIGINT UNSIGNED', remarks: 'devops_vuln_scan_record.id') {
                constraints(nullable: false)
            }
            column(name: 'target', type: 'VARCHAR(512)', remarks: '扫描对象') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_vuln_scan_target', indexName: 'devops_vuln_scan_target_n1') {
            column(name: 'scan_record_id')
        }

    }
}
