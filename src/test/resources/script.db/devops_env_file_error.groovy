package script.db

/**
 * Creator: Runge
 * Date: 2018/8/9
 * Time: 20:33
 * Description: store error file in GitOps
 */

databaseChangeLog(logicalFilePath: 'db/devops_env_file_error.groovy') {
    changeSet(author: 'Runge', id: '2018-08-09-create-table') {
        createTable(tableName: "devops_env_file_error", remarks: '环境错误文件') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID ')
            column(name: 'file_path', type: 'VARCHAR(512)', remarks: '文件路径')
            column(name: 'commit', type: 'VARCHAR(100)', remarks: '提交')
            column(name: 'error', type: 'VARCHAR(2000)', remarks: '报错信息')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2018-08-09-modify-column', author: 'younger') {
        modifyDataType(tableName: 'devops_env_file_error', columnName: 'error', newDataType: 'VARCHAR(5000)')
    }
}