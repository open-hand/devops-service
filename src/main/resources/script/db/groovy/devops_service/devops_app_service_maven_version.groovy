package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_service_maven_version.groovy') {
    changeSet(author: 'wanghao', id: '2022-07-13-create-table') {
        createTable(tableName: "devops_app_service_maven_version", remarks: '应用版本表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_version_id', type: 'BIGINT UNSIGNED', remarks: '应用服务版本，devops_app_service_version.id') {
                constraints(nullable: false)
            }
            column(name: "group_id", type: "VARCHAR(60)", remarks: "groupId") {
                constraints(nullable: false)
            }
            column(name: "artifact_id", type: "VARCHAR(60)", remarks: "artifactId") {
                constraints(nullable: false)
            }
            column(name: "version", type: "VARCHAR(60)", remarks: "版本") {
                constraints(nullable: false)
            }
            column(name: "nexus_repo_id", type: "BIGINT UNSIGNED", remarks: "nexus仓库id,hrds_prod_repo.rdupm_nexus_repository.repository_id")
            column(name: "maven_repo_url", type: "VARCHAR(255)")
            column(name: "username", type: "VARCHAR(255)")
            column(name: "password", type: "VARCHAR(255)")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_app_service_maven_version',
                constraintName: 'devops_app_service_maven_version_u1', columnNames: 'app_service_version_id')
    }
    changeSet(author: 'wanghao', id: '2022-12-15-updateDataType') {
        modifyDataType(tableName: 'devops_app_service_maven_version', columnName: 'version', newDataType: 'VARCHAR(256)')
    }
}