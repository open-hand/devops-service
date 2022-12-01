package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_audit_user.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-02-create-table') {
        createTable(tableName: "devops_pipeline_audit_user", remarks: '人工卡点审核人员表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'audit_config_id', type: 'BIGINT UNSIGNED', remarks: 'devops_pipeline_audit_cfg.id') {
                constraints(nullable: false)
            }
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_pipeline_audit_user',
                constraintName: 'devops_pipeline_audit_user_u1', columnNames: 'audit_config_id,user_id')
    }

}