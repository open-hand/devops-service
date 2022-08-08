package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_service_helm_rel.groovy') {
    changeSet(author: 'wanghao', id: '2022-07-15-create-table') {
        createTable(tableName: "devops_app_service_helm_rel", remarks: '应用服务和helm配置的关联关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id，devops_app_service.id') {
                constraints(nullable: false)
            }
            column(name: 'helm_config_id', type: 'BIGINT UNSIGNED', remarks: '配置Id,devops_helm_config.id') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_app_service_helm_rel',
                constraintName: 'devops_app_service_helm_rel_u1', columnNames: 'app_service_id')
    }
}