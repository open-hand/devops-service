package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_vuln_scan_record.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_vuln_scan_record", remarks: '漏洞扫描记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id') {
                constraints(nullable: false)
            }
            column(name: 'branch_name', type: 'VARCHAR(255)', remarks: '分支名') {
                constraints(nullable: false)
            }
            column(name: 'unknown', type: 'BIGINT UNSIGNED', remarks: '未知漏洞数')
            column(name: 'low', type: 'BIGINT UNSIGNED', remarks: '较低漏洞数')
            column(name: 'medium', type: 'BIGINT UNSIGNED', remarks: '中等漏洞数')
            column(name: 'high', type: 'BIGINT UNSIGNED', remarks: '严重漏洞数')
            column(name: 'critical', type: 'BIGINT UNSIGNED', remarks: '危急漏洞数')
            column(name: 'score', type: 'BIGINT UNSIGNED', remarks: '评分')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_vuln_scan_record', indexName: 'devops_vlun_scan_record_n1') {
            column(name: 'app_service_id')
        }

    }
}
