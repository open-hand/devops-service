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
    changeSet(author: 'wanghao', id: '2021-12-15-add-column') {
        addColumn(tableName: 'devops_image_scan_result') {
            column(name: "app_service_id", type: "BIGINT UNSIGNED", afterColumn: 'VULNERABILITY_CODE')
            column(name: "job_name", type: "VARCHAR(255)", remarks: "任务名称")
        }
    }
    changeSet(author: 'wanghao', id: '2021-12-15-fix-data') {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "devops_ci_pipeline_record")
        }
        sql("""
            update devops_image_scan_result disr
            SET disr.app_service_id = 
            (SELECT dcp.app_service_id 
                FROM devops_ci_pipeline_record dcpr 
                JOIN devops_cicd_pipeline dcp on dcpr.ci_pipeline_id = dcp.id
                WHERE dcpr.gitlab_pipeline_id = disr.gitlab_pipeline_id 
                limit 1)
        """)
    }
    changeSet(author: 'wanghao', id: '2021-12-15-modify-unique-index') {
        addUniqueConstraint(tableName: 'devops_image_scan_result',
                constraintName: 'uk_devops_gitlab_pipeline_id', columnNames: 'app_service_id,gitlab_pipeline_id,job_name')
    }

}