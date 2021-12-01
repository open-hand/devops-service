package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_job.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_job') {
        createTable(tableName: "devops_ci_template_job", remarks: '流水线任务模板表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(60)', remarks: '任务名称') {
                constraints(nullable: false)
            }
            column(name: 'group_id',  type: 'BIGINT UNSIGNED', remarks: '任务分组id') {
                constraints(nullable: false)
            }
            column(name: 'runner_images',  type: 'VARCHAR(500)', remarks: '流水线模板镜像地址') {
                constraints(nullable: false)
            }
            column(name: 'source_type', type: 'VARCHAR(12)', remarks: '层级') {
                constraints(nullable: false)
            }
            column(name: 'source_id',  type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(20)', remarks: '步骤类型') {
                constraints(nullable: false)
            }


            column(name: 'to_upload', type: 'TINYINT UNSIGNED', remarks: '是否上传到共享目录') {
                constraints(nullable: false)
            }

            column(name: 'to_download', type: 'TINYINT UNSIGNED', remarks: '是否下载到共享目录') {
                constraints(nullable: false)
            }




            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }

    }
}