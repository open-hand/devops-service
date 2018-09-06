package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_template.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_app_template", remarks: '应用模板') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'organization_id', type: 'BIGINT UNSIGNED', remarks: '组织 ID')
            column(name: 'gitlab_project_id', type: 'BIGINT UNSIGNED', remarks: 'GitLab 项目 ID')
            column(name: 'name', type: 'VARCHAR(32)', remarks: '模板名称')
            column(name: 'code', type: 'VARCHAR(32)', remarks: '模板编码')
            column(name: 'type', type: 'TINYINT UNSIGNED', remarks: '模板类型')
            column(name: 'copy_from', type: 'BIGINT UNSIGNED', remarks: '复制于')
            column(name: 'repo_url', type: 'VARCHAR(128)', remarks: '模板地址')
            column(name: 'uuid', type: 'VARCHAR(50)')
            column(name: 'description', type: 'VARCHAR(128)', remarks: '模板描述')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_organization_id ", tableName: "devops_app_template") {
            column(name: "organization_id")
        }
        addUniqueConstraint(tableName: 'devops_app_template',
                constraintName: 'uk_org_id_name', columnNames: 'organization_id,name')
        addUniqueConstraint(tableName: 'devops_app_template',
                constraintName: 'uk_org_id_code', columnNames: 'organization_id,code')
    }

}