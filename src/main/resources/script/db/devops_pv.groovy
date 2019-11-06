package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pv') {
    changeSet(id: '2019-11-1-add-devops_pv', author: 'yzj') {
        createTable(tableName: "devops_pv") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'varchar(30)', remarks: 'pv名称')
            column(name: 'type', type: 'varchar(10)', remarks: 'pv存储类型')
            column(name: 'description', type: 'varchar(40)', remarks: 'pv描述')
            column(name: 'status', 'type':'varchar(32)' ,remarks: 'pv状态')
            column(name: 'pvc_id', type: 'BIGINT UNSIGNED', remarks: '关联的pvc')
            column(name: 'cluster_id',type: 'BIGINT UNSIGNED', remarks: '所属集群')
            column(name: 'storage', type: 'varchar(20)',remarks: '存储容量')
            column(name: 'access_modes', type: 'varchar(20)', remarks: '访问类型')
            column(name: 'skip_check_project_permission', type: 'tinyint(1)',defaultValue: 1,remarks: '指定权限,是否只属于特定项目，默认为1')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_pv',
                constraintName: 'uk_name_cluster', columnNames: 'name, cluster_id')
        createIndex(indexName: 'idx_name', tableName: 'devops_pv') {
            column(name: 'name')
        }
    }

    changeSet(id: '2019-11-06-rename-column', author: 'yzj') {
        renameColumn(columnDataType: 'varchar(20)', newColumnName: 'request_resource', oldColumnName: 'storage', remarks: '存储容量', tableName: 'devops_pv')
        addColumn(tableName: 'devops_pv') {
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作id')
        }
    }
}