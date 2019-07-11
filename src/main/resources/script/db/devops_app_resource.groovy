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
}