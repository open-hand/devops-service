package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_stage.groovy') {
    changeSet(author: 'wanghao', id: '2020-06-30-create-table') {
        createTable(tableName: "devops_cd_stage", remarks: 'CD阶段表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '阶段名称') {
                constraints(nullable: false)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '阶段顺序') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(32)', remarks: 'ci的还是cd的阶段')

            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'wanghao', id: '2023-03-28-drop-create-table') {
        dropTable(tableName: "devops_cd_stage")
    }
}