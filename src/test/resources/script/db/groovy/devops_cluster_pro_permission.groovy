package script.db.groovy


databaseChangeLog(logicalFilePath: 'dba/devops_cluster_pro_permission.groovy') {
    changeSet(author: 'Younger', id: '2018-11-01-create-table') {
        createTable(tableName: "devops_cluster_pro_permission", remarks: 'cluster project permission') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true)
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '集群Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: 'project_name', type: 'VARCHAR(64)', remarks: '项目名')
            column(name: 'project_code', type: 'VARCHAR(64)', remarks: '项目编码')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


    changeSet(author: 'younger', id: '2018-11-21-add-column') {
        dropColumn(columnName: "project_name", tableName: "devops_cluster_pro_permission")
        dropColumn(columnName: "project_code", tableName: "devops_cluster_pro_permission")
        renameTable(newTableName: 'devops_cluster_pro_rel', oldTableName: 'devops_cluster_pro_permission')
    }

    changeSet(author: 'zmf', id: '2020-01-13-add-primary-key') {
        addNotNullConstraint(tableName: "devops_cluster_pro_rel", columnName: "cluster_id", columnDataType: "BIGINT UNSIGNED")
        addNotNullConstraint(tableName: "devops_cluster_pro_rel", columnName: "project_id", columnDataType: "BIGINT UNSIGNED")

        addUniqueConstraint(tableName: 'devops_cluster_pro_rel',
                constraintName: 'devops_cluster_pro_cluster_id_project_id_uk', columnNames: 'cluster_id, project_id')
    }
}