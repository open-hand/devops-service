package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_application.groovy') {
    changeSet(author: 'lizongwei', id: '2019-07-01-create-table') {
        createTable(tableName: "devops_env_application", remarks: '应用环境关联') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
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

    changeSet(author: 'zmf', id: '2019-08-06-rename-table') {
        renameTable(newTableName: 'devops_env_app_service', oldTableName: 'devops_env_application')
    }

    changeSet(author: 'zmf', id: '2020-05-13-devops_env_app_service-add-pk') {
        dropPrimaryKey(tableName: 'devops_env_app_service')
        addUniqueConstraint(tableName: 'devops_env_app_service',
                constraintName: 'uk_devops_env_app_service_env_id_app_id', columnNames: 'env_id,app_service_id')
    }
}
