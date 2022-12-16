package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_stage_record.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-23-create-table') {
        createTable(tableName: "devops_pipeline_stage_record", remarks: '流水线阶段记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '名称') {
                constraints(nullable: false)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '所属流水线Id,devops_pipeline.id') {
                constraints(nullable: false)
            }
            column(name: 'stage_id', type: 'BIGINT UNSIGNED', remarks: '所属阶段Id,devops_pipeline_stage.id') {
                constraints(nullable: false)
            }
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '阶段顺序', defaultValue: "0") {
                constraints(nullable: false)
            }
            column(name: 'pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '关联流水线记录Id,devops_pipeline_record.id') {
                constraints(nullable: false)
            }
            column(name: 'next_stage_record_id', type: 'BIGINT UNSIGNED', remarks: '下一个阶段记录id,devops_pipeline_stage_record.id')
            column(name: 'status', type: 'VARCHAR(20)', remarks: '状态') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_pipeline_stage_record', indexName: 'devops_stage_record_n1') {
            column(name: 'pipeline_record_id')
        }
        createIndex(tableName: 'devops_pipeline_stage_record', indexName: 'devops_stage_record_n2') {
            column(name: 'pipeline_id')
        }
    }

}
