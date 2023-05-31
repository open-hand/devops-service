package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_tpl_vuln_scan_config.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_ci_tpl_vuln_scan_config", remarks: '流水线模板ci 漏洞扫描配置信息表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_template_step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤id') {
                constraints(nullable: false)
            }
            column(name: 'path', type: 'VARCHAR(256)', remarks: '扫描目录或文件') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_tpl_vuln_scan_config',
                constraintName: 'uk_step_id', columnNames: 'ci_template_step_id')
    }

}