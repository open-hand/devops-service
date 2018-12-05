package script.db


databaseChangeLog(logicalFilePath: 'dba/devops_config_map.groovy') {
    changeSet(author: 'Younger', id: '2018-12-03-create-table') {
        createTable(tableName: "devops_config_map", remarks: 'config map') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: 'env Id')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'command Id')
            column(name: 'name', type: 'VARCHAR(32)', remarks: 'name')
            column(name: 'description', type: 'VARCHAR(64)', remarks: '信息')
            column(name: 'value', type: 'TEXT', remarks: 'key-value值')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_config_map',
                constraintName: 'uk_env_id_name', columnNames: 'env_id,name')
    }
}