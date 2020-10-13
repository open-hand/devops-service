package script.db.groovy

databaseChangeLog(logicalFilePath: 'devops_prometheus.groovy') {
    changeSet(id: '2019-10-28-add-devops_prometheus', author: 'lizhaozhong') {
        createTable(tableName: "devops_prometheus") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'admin_password', type: 'VARCHAR(50)', remarks: 'admin密码')
            column(name: 'grafana_domain', type: 'VARCHAR(50)', remarks: 'grafana的域名地址')
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: 'cluster id')
            column(name: 'prometheus_pv_id', type: 'BIGINT UNSIGNED', remarks: 'PrometheusPv id')
            column(name: 'grafana_pv_id', type: 'BIGINT UNSIGNED', remarks: 'GrafanaPv id')
            column(name: 'alertmanager_pv_id', type: 'BIGINT UNSIGNED', remarks: 'AlertmanagerPv id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
    }

    changeSet(author: 'lihao', id: '2020-07-16-add-column') {
        addColumn(tableName: 'devops_prometheus') {
            column(name: 'enable_tls', type: 'tinyint(1)', remarks: '是否启用tls', afterColumn: 'cluster_id', defaultValue: 0)
        }
    }

}