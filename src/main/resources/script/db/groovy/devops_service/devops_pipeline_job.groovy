package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_job.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-23-create-table') {
        createTable(tableName: "devops_pipeline_job", remarks: '流水线任务表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '所属流水线Id,devops_pipeline.id') {
                constraints(nullable: false)
            }
            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '所属版本Id,devops_pipeline_version.id') {
                constraints(nullable: false)
            }
            column(name: 'stage_id', type: 'BIGINT UNSIGNED', remarks: '所属阶段Id,devops_pipeline_stage.id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '名称') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(64)', remarks: '任务类型') {
                constraints(nullable: false)
            }
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '关联任务配置Id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_pipeline_job', indexName: 'devops_pipeline_job_n1') {
            column(name: 'pipeline_id')
        }
        createIndex(tableName: 'devops_pipeline_job', indexName: 'devops_pipeline_job_n2') {
            column(name: 'version_id')
        }
        createIndex(tableName: 'devops_pipeline_job', indexName: 'devops_pipeline_job_n3') {
            column(name: 'config_id')
        }
        addUniqueConstraint(tableName: 'devops_pipeline_job',
                constraintName: 'devops_pipeline_job_u1', columnNames: 'stage_id,name')
    }
    changeSet(author: 'wanghao', id: '2023-04-03-add-column') {
        addColumn(tableName: 'devops_pipeline_job') {
            column(name: "is_enabled", type: "TINYINT UNSIGNED", defaultValue: "1", afterColumn: 'config_id', remarks: '是否启用')
        }
    }

}
