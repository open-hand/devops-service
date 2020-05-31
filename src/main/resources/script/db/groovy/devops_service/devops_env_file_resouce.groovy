package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'db/devops_env_file_resource.groovy') {
    changeSet(author: 'Runge', id: '2018-07-25-create-table') {
        createTable(tableName: "devops_env_file_resource", remarks: '环境文件信息') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID ') {
                constraints(unique: true)
            }
            column(name: 'file_path', type: 'VARCHAR(512)', remarks: '文件路径')
            column(name: 'resource_type', type: 'VARCHAR(32)', remarks: '资源类型')
            column(name: 'resource_id', type: 'BIGINT UNSIGNED', remarks: '资源ID')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2018-08-01-drop-constraint', author: 'runge') {
        dropUniqueConstraint(tableName: 'devops_env_file_resource',
                constraintName: 'env_id')
    }

    changeSet(id: '2019-10-31-add-unique-constraint', author: 'zmf') {
        sql("""
            DELETE devops_env_file_resource.*
FROM devops_env_file_resource
WHERE (env_id, resource_type, resource_id) IN (SELECT tmp.* FROM (SELECT duplication.env_id, duplication.resource_type, duplication.resource_id
                                               FROM devops_env_file_resource duplication
                                               GROUP BY duplication.env_id, duplication.resource_type, duplication.resource_id
                                               HAVING COUNT(1) > 1) tmp)
  AND id NOT IN (SELECT tmp2.* FROM (SELECT MIN(id) as remain_id
                 FROM devops_env_file_resource remain
                 GROUP BY remain.env_id, remain.resource_type, remain.resource_id
                 HAVING COUNT(1) > 1) tmp2);
            """)
        addUniqueConstraint(tableName: 'devops_env_file_resource',
                constraintName: 'file_resource_uk_env_type_resource_id', columnNames: 'env_id, resource_type, resource_id')
    }
}