package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_service_helm_version.groovy') {
    changeSet(author: 'wanghao', id: '2022-07-13-create-table') {
        createTable(tableName: "devops_app_service_helm_version", remarks: '应用版本表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_version_id', type: 'BIGINT UNSIGNED', remarks: '应用服务版本，devops_app_service_version.id') {
                constraints(nullable: false)
            }
            column(name: 'helm_config_id', type: 'BIGINT UNSIGNED', remarks: '配置Id,devops_helm_config.id') {
                constraints(nullable: false)
            }
            column(name: 'harbor_repo_type', type: 'VARCHAR(64)', remarks: '仓库类型(DEFAULT_REPO、CUSTOM_REPO)') {
                constraints(nullable: false)
            }
            column(name: 'harbor_config_id', type: 'BIGINT UNSIGNED', remarks: 'harbor仓库配置Id,hrds_prod_repo.rdupm_harbor_repository.id/hrds_prod_repo.rdupm_harbor_custom_repo.id') {
                constraints(nullable: false)
            }
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: 'chart包values ID,devops_app_service_version_value.id') {
                constraints(nullable: false)
            }
            column(name: 'readme_value_id', type: 'BIGINT UNSIGNED', remarks: 'readme value id,devops_app_service_version_readme.id') {
                constraints(nullable: false)
            }
            column(name: 'image', type: 'VARCHAR(255)', remarks: '镜像名') {
                constraints(nullable: false)
            }
            column(name: 'repository', type: 'VARCHAR(255)', remarks: '仓库地址') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_app_service_helm_version',
                constraintName: 'devops_app_service_helm_version_u1', columnNames: 'app_service_version_id')
    }
    changeSet(author: 'wanghao', id: '2023-03-10-updateDataType') {
        dropNotNullConstraint(tableName: 'devops_app_service_helm_version', columnName: 'harbor_repo_type')
        dropNotNullConstraint(tableName: 'devops_app_service_helm_version', columnName: 'harbor_config_id')
        dropNotNullConstraint(tableName: 'devops_app_service_helm_version', columnName: 'image')
    }
}