package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_versionion.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_app_version", remarks: '应用版本表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'version', type: 'VARCHAR(64)', remarks: '版本号')
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用 ID')
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: '参数 ID')
            column(name: 'image', type: 'VARCHAR(255)', remarks: '镜像名')
            column(name: 'commit', type: 'CHAR(40)', remarks: 'commit Hash或Tag')
            column(name: 'repository', type: 'VARCHAR(255)', remarks: '仓库地址')
            column(name: 'is_publish', type: 'TINYINT UNSIGNED', remarks: '是否发布', defaultValue: "0")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_app_version',
                constraintName: 'uk_app_id_version', columnNames: 'app_id,version')
        createIndex(indexName: "idx_app_id", tableName: "devops_app_version") {
            column(name: "app_id")
        }
    }


    changeSet(author: 'younger', id: '2018-09-03-modify-index') {
        dropIndex(indexName: "idx_app_id", tableName: "devops_app_version")

        createIndex(indexName: "app_version_idx_app_id", tableName: "devops_app_version") {
            column(name: "app_id")
        }
    }

    changeSet(author: 'younger', id: '2018-10-08-add-column') {
        addColumn(tableName: 'devops_app_version') {
            column(name: 'readme_value_id', type: 'BIGINT UNSIGNED', remarks: 'readme value id', afterColumn: 'value_id')
        }
    }
}