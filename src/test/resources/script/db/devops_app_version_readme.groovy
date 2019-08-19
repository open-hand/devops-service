package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_version_readme.groovy') {
    changeSet(author: 'Runge', id: '2018-06-19-create-table') {
        createTable(tableName: "devops_app_version_readme", remarks: '应用版本readme') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '版本 ID') {
                constraints(unique: true)
            }
            column(name: 'readme', type: 'TEXT', remarks: 'README')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }



    changeSet(author: 'Runge', id: '2018-10-08-drop-column') {
        dropColumn(columnName: "object_version_number", tableName: "devops_app_version_readme")
        dropColumn(columnName: "created_by", tableName: "devops_app_version_readme")
        dropColumn(columnName: "creation_date", tableName: "devops_app_version_readme")
        dropColumn(columnName: "last_updated_by", tableName: "devops_app_version_readme")
        dropColumn(columnName: "last_update_date", tableName: "devops_app_version_readme")
        dropColumn(columnName: "version_id", tableName: "devops_app_version_readme")

    }


    changeSet(author: 'Sheep', id: '2019-07-05-updateDataType') {
        modifyDataType(tableName: 'devops_app_version_readme', columnName: 'readme', newDataType: 'MEDIUMTEXT')
    }

    changeSet(author: 'sheep', id: '2019-8-05-rename-table') {
        renameTable(newTableName: 'devops_app_service_version_readme', oldTableName: 'devops_app_version_readme')
    }
}