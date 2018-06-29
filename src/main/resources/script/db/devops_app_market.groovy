package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_market.groovy') {
    changeSet(author: 'ernst', id: '2018-05-12-create-table') {
        createTable(tableName: "devops_app_market", remarks: '应用市场') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用 ID')
            column(name: 'contributor', type: 'VARCHAR(15)', remarks: '贡献者')
            column(name: 'description', type: 'VARCHAR(100)', remarks: '描述')
            column(name: 'img_url', type: 'VARCHAR(200)', remarks: '图标url')
            column(name: 'publish_level', type: 'VARCHAR(30)', remarks: '发布的层级')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "idx_app_id", tableName: "devops_app_market") {
            column(name: "app_id")
        }

        addUniqueConstraint(tableName: 'devops_app_market',
                constraintName: 'uk_app_id', columnNames: 'app_id')
    }

    changeSet(author: 'ernst', id: '2018-05-22-add-column') {
        addColumn(tableName: 'devops_app_market') {
            column(name: 'category', type: 'VARCHAR(30)', remarks: '类别')
            column(name: 'is_active', type: 'TINYINT UNSIGNED', remarks: '是否有效')
        }
    }

    changeSet(author: 'Runge', id: '2018-06-29-updateDataType') {
        modifyDataType(tableName: 'devops_app_market', columnName: 'contributor', newDataType: 'VARCHAR(128)')
    }

}
