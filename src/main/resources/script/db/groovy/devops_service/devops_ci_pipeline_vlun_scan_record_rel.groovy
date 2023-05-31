package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_vlun_scan_record_rel.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_ci_pipeline_vlun_scan_record_rel", remarks: 'ci流水线漏洞扫描记录关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id') {
                constraints(nullable: false)
            }
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlabPipelineId') {
                constraints(nullable: false)
            }
            column(name: "job_name", type: "VARCHAR(255)", remarks: "任务名称") {
                constraints(nullable: false)
            }
            column(name: 'scan_record_id', type: 'BIGINT UNSIGNED', remarks: 'devops_vuln_scan_record.id') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_vlun_scan_record_rel',
                constraintName: 'uk_devops_gitlab_pipeline_id', columnNames: 'app_service_id,gitlab_pipeline_id,job_name')
    }
}
