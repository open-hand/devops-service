package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_host_app_instance_rel.groovy') {
    changeSet(author: 'wanghao', id: '2021-07-14-create-devops_host_app_instance_rel') {
        createTable(tableName: "devops_host_app_instance_rel", remarks: '主机应用实例关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: 'host_id', type: 'BIGINT UNSIGNED', remarks: '主机id') {
                constraints(nullable: false)
            }
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例id') {
                constraints(nullable: false)
            }
            column(name: 'instance_type', type: 'VARCHAR(32)', remarks: '实例类型') {
                constraints(nullable: false)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id') {
                constraints(nullable: false)
            }
            column(name: 'app_source', type: 'VARCHAR(32)', remarks: '应用来源') {
                constraints(nullable: false)
            }
            column(name: 'service_name', type: 'VARCHAR(64)', remarks: '应用名称') {
                constraints(nullable: false)
            }



            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_host_app_instance_rel',
                constraintName: 'uk_app_instance', columnNames: 'instance_id,app_id')
    }

}