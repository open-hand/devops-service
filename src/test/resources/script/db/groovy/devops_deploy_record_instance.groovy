package script.db.groovy


databaseChangeLog(logicalFilePath: 'dba/devops_deploy_record_instance.groovy') {
    changeSet(author: 'zmf', id: '2020-02-26-create-table-devops_deploy_record_instance') {
        createTable(tableName: "devops_deploy_record_instance", remarks: '部署纪录及实例关联表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，环境ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'deploy_record_id', type: 'BIGINT UNSIGNED', remarks: '部署纪录id') {
                constraints(nullable: false)
            }
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例id') {
                constraints(nullable: false)
            }
            column(name: 'instance_code', type: 'VARCHAR(64)', remarks: '实例code') {
                constraints(nullable: false)
            }
            column(name: 'instance_version', type: 'VARCHAR(64)', remarks: '实例部署时的版本') {
                constraints(nullable: false)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id') {
                constraints(nullable: false)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id') {
                constraints(nullable: false)
            }


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_deploy_record_instance',
                constraintName: 'deploy_record_instance_record_id_instance_id', columnNames: 'deploy_record_id, instance_id')
    }

}