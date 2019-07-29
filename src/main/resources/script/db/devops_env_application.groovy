package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_application.groovy') {
    changeSet(author: 'lizongwei', id: '2019-07-01-create-table') {
        createTable(tableName: "devops_env_application", remarks: '应用环境关联') {

            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID '){
                constraints(primaryKey: true)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用 ID'){
                constraints(primaryKey: true)
            }
        }
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_env_application')
    }
}
