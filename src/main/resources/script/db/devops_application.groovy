package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_application.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_application", remarks: '应用管理') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目 ID')
            column(name: 'name', type: 'VARCHAR(64)', remarks: '应用名称')
            column(name: 'code', type: 'VARCHAR(64)', remarks: '应用编码')
            column(name: 'is_active', type: 'TINYINT UNSIGNED', remarks: '同步状态')
            column(name: 'is_synchro', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: '是否同步成功。1成功，0失败')
            column(name: 'gitlab_project_id', type: 'BIGINT UNSIGNED', remarks: 'GitLab 项目 ID')
            column(name: 'app_template_id', type: 'BIGINT UNSIGNED', remarks: '应用模板 ID')
            column(name: 'uuid', type: 'VARCHAR(50)')
            column(name: 'token', type: 'CHAR(36)', remarks: 'TOKEN')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_project_id ", tableName: "devops_application") {
            column(name: "project_id")
        }
//        addUniqueConstraint(tableName: 'devops_application',
//                constraintName: 'uk_project_id_name', columnNames: 'project_id,name')
    }

    changeSet(author: 'younger', id: '2018-07-11-add-column') {
        addColumn(tableName: 'devops_application') {
            column(name: 'hook_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab webhook', afterColumn: 'gitlab_project_id')
        }

    }
}