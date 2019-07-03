package script.db


databaseChangeLog(logicalFilePath: 'dba/devops_customize_resource.groovy') {
    changeSet(author: 'Sheep', id: '2019-06-26-create-table') {
        createTable(tableName: "devops_customize_resource", remarks: 'customize resource') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: 'project Id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: 'env Id')
            column(name: 'content_id', type: 'BIGINT UNSIGNED', remarks: 'content id')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'command id')
            column(name: 'k8s_kind', type: 'VARCHAR(32)', remarks: 'k8s resource kind')
            column(name: 'name', type: 'VARCHAR(32)', remarks: 'k8s resource name')
            column(name: 'file_path', type: 'VARCHAR(16)', remarks: 'gitOps file path')
            column(name: 'description', type: 'VARCHAR(5000)', remarks: 'k8s resource description')


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_customize_resource',
                constraintName: 'uk_env_id_kind_name', columnNames: 'env_id,k8s_kind,name')
    }

}