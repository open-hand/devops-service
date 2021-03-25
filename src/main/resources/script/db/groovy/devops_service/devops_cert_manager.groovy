package script.db.groovy.devops_service

/**
 * @author zhaotianxin* @since 2019/11/1
 */
databaseChangeLog(logicalFilePath: 'dba/devops_cert_manager.groovy') {
    changeSet(author: 'ztx', id: '2019-11-01-create-table') {
        createTable(tableName: "devops_cert_manager", remarks: 'cert-manager 信息') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'email', type: 'VARCHAR(128)', remarks: '邮箱')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: '命名空间')
            column(name: 'chart_version', type: 'VARCHAR(128)', remarks: 'chart版本')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'zmf', id: "2021-03-25-add-cert-manager-name") {
        addColumn(tableName: "devops_cert_manager") {
            column(name: 'release_name', type: 'VARCHAR(128)', remarks: 'CertManager的实例名称', defaultValue: 'choerodon-cert-manager', afterColumn: 'id')
        }
    }
}