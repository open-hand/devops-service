package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_vuln_target_rel.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_vuln_target_rel", remarks: '漏洞扫描对象关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'target_id', type: 'BIGINT UNSIGNED', remarks: 'devops_vuln_scan_target.id') {
                constraints(nullable: false)
            }
            column(name: 'pkg_name', type: 'VARCHAR(255)', remarks: '组件名称') {
                constraints(nullable: false)
            }
            column(name: "installed_version", type: 'VARCHAR(64)', remarks: '当前版本') {
                constraints(nullable: false)
            }
            column(name: "fixed_version", type: 'VARCHAR(64)', remarks: '修复版本')
            column(name: 'vulnerability_id', type: 'VARCHAR(255)', remarks: '漏洞id') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_vuln_target_rel', indexName: 'devops_vuln_target_rel_n1') {
            column(name: 'target_id')
        }
    }
}
