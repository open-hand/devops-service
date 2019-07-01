package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_application.groovy') {
    changeSet(author: 'lizongwei', id: '2019-07-01-create-table') {
        createTable(tableName: "devops_env_application", remarks: '应用环境关联') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用 ID')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID ')
        }

        createIndex(indexName: "idx_env_id", tableName: "devops_env_application") {
            column(name: "app_id")
        }

    }
}
