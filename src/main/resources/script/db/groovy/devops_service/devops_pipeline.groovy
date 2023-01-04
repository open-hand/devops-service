package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-23-create-table') {
        createTable(tableName: "devops_pipeline", remarks: '流水线表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '流水线名称') {
                constraints(nullable: false)
            }
            column(name: 'effect_version_id', type: 'BIGINT UNSIGNED', remarks: '当前生效的版本，devops_pipeline_version.id')
            column(name: 'token', type: 'VARCHAR(256)', remarks: '令牌') {
                constraints(nullable: false)
            }
            column(name: "is_enable", type: "TINYINT UNSIGNED", defaultValue: "1", remarks: '是否启用')
            column(name: 'is_app_version_trigger_enable', type: 'TINYINT UNSIGNED', defaultValue: "1", remarks: '是否启用应用服务版本生成触发')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_pipeline', indexName: 'devops_pipeline_n1') {
            column(name: 'name')
        }
        addUniqueConstraint(tableName: 'devops_pipeline',
                constraintName: 'devops_pipeline_u1', columnNames: 'token')
        addUniqueConstraint(tableName: 'devops_pipeline',
                constraintName: 'devops_pipeline_u2', columnNames: 'project_id,name')
    }

}
