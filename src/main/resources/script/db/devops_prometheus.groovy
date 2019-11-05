package script.db

databaseChangeLog(logicalFilePath: 'devops_prometheus.groovy') {
    changeSet(id: '2019-10-28-add-devops_prometheus', author: 'lizhaozhong') {
        createTable(tableName: "devops_prometheus") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'admin_password', type: 'VARCHAR(50)', remarks: 'admin密码')
            column(name: 'grafana_domain', type: 'VARCHAR(50)', remarks: 'grafana的域名地址')
            column(name: 'pv_name', type: 'VARCHAR(50)', remarks: 'grafana的域名地址')
            column(name: 'cluster_name', type: 'VARCHAR(50)', remarks: '集群名称')


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'scp', id: '2019-11-04-drop-column') {
        dropColumn(columnName: "cluster_name", tableName: "devops_prometheus")
    }

    changeSet(author: 'lzz', id: '2019-11-05-update-column') {
        renameColumn(columnDataType: 'VARCHAR(50)', newColumnName: 'pv_names', oldColumnName: 'pv_name',tableName: "devops_prometheus")
        addColumn(tableName: 'devops_cluster_resource') {
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: 'cluster id', afterColumn: 'grafana_domain')
        }
    }
}