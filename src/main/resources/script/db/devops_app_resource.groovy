package script.db

/**
 * @author lizongwei
 * @date 2019/7/2
 */
databaseChangeLog(logicalFilePath: 'dba/devops_app_resource.groovy') {
    changeSet(author: 'lizongwei', id: '2019-07-02-create-table') {
        createTable(tableName: "devops_app_resource", remarks: '实例') {

            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用 ID') {
                constraints(primaryKey: true)
            }
            column(name: 'resource_type', type: 'VARCHAR(20)', remarks: '资源类型') {
                constraints(nullable: false)
            }
            column(name: 'resource_id', type: 'BIGINT UNSIGNED', remarks: '资源 ID') {
                constraints(primaryKey: true)
            }

        }

        createIndex(indexName: "idx_appid_resourcetype ", tableName: "devops_app_resource") {
            column(name: "app_id")
            column(name: "resource_type")
        }

    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_app_resource')
    }


    changeSet(author: 'zmf', id: '2019-08-06-rename-table') {
        renameTable(newTableName: 'devops_app_service_resource', oldTableName: 'devops_app_resource')
    }
}