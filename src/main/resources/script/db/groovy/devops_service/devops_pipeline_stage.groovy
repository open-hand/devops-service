package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_stage.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-23-create-table') {
        createTable(tableName: "devops_pipeline_stage", remarks: '流水线阶段表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '所属流水线Id,devops_pipeline.id') {
                constraints(nullable: false)
            }
            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '所属版本Id,devops_pipeline_version.id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '名称') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_pipeline_stage', indexName: 'devops_pipeline_stage_n1') {
            column(name: 'pipeline_id')
        }
        createIndex(tableName: 'devops_pipeline_stage', indexName: 'devops_pipeline_stage_n2') {
            column(name: 'version_id')
        }
        addUniqueConstraint(tableName: 'devops_pipeline_stage',
                constraintName: 'devops_pipeline_stage_u1', columnNames: 'version_id,name')
    }

}
