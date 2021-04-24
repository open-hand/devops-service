package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_image_scan_result.groovy') {
    changeSet(author: 'wx', id: '2021-03-25-devops_image_scan_result') {
        createTable(tableName: "devops_image_scan_result", remarks: '实例') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
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
            column(name: "DESCRIPTION", type: 'text', remarks: '简介')
            column(name: "TARGET", type: 'VARCHAR(120)', remarks: '镜像名称')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

    }

    changeSet(author: 'wx', id: '2021-04-21-devops_image_scan_result') {
        sql("""
           alter table devops_image_scan_result modify VULNERABILITY_CODE varchar(20) null;
           alter table devops_image_scan_result modify PKG_NAME varchar(64) null;
           alter table devops_image_scan_result modify INSTALLED_VERSION varchar(64) null;
        """)

    }
}