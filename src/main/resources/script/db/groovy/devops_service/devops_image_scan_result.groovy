package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_image_scan_result.groovy') {
    changeSet(author: 'wx', id: '2021-03-25-devops_image_scan_result') {
        createTable(tableName: "devops_image_scan_result", remarks: '实例') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'JOB_ID', type: 'BIGINT UNSIGNED', remarks: 'jobId') {
                constraints(nullable: false)
            }

            column(name: 'VULNERABILITY_CODE', type: 'VARCHAR(20)', remarks: '漏洞码') {
                constraints(nullable: false)
            }
            column(name: 'GITLAB_PIPELINE_ID', type: 'BIGINT UNSIGNED', remarks: 'gitlab_流水线记录id') {
                constraints(nullable: false)
            }
            column(name: "START_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "END_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "SEVERITY", type: 'VARCHAR(8)', remarks: '漏洞等级', defaultValue: 'UNKNOWN')
            column(name: 'PKG_NAME', type: 'VARCHAR(64)', remarks: '组件名称') {
                constraints(nullable: false)
            }
            column(name: "INSTALLED_VERSION", type: 'VARCHAR(64)', remarks: '当前版本') {
                constraints(nullable: false)
            }
            column(name: "FIXED_VERSION", type: 'VARCHAR(64)', remarks: '修复版本')
            column(name: "DESCRIPTION", type: 'VARCHAR(64)', remarks: '简介')
            column(name: "TARGET", type: 'VARCHAR(120)', remarks: '镜像名称')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

    }
}