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

        createIndex(indexName: "app_market_idx_app_id", tableName: "devops_app_market") {
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

    changeSet(author: 'younger', id: '2018-09-03-modify-index') {
        dropIndex(indexName: "app_market_idx_app_id", tableName: "devops_app_market")

        createIndex(indexName: "devops_market_idx_app_id", tableName: "devops_app_market") {
            column(name: "app_id")
        }
    }
    changeSet(author: 'scp', id: '2019-06-28-add-column') {
        addColumn(tableName: 'devops_app_market') {
            column(name: 'is_free', type: 'TINYINT UNSIGNED', remarks: '是否收费', afterColumn: "publish_level", defaultValue: "1")
        }
    }

    changeSet(id: '2019-06-28-rename-table', author: 'scp') {
        renameTable(newTableName: 'devops_app_share', oldTableName: 'devops_app_market')
    }

    changeSet(author: 'scp', id: '2019-07-02-add-column') {
        addColumn(tableName: 'devops_app_share') {
            column(name: 'is_site', type: 'TINYINT UNSIGNED', remarks: '是否发布到平台层', afterColumn: "publish_level", defaultValue: "0")
        }
    }

    changeSet(id: '2019-07-24-rename-table', author: 'scp') {
        dropColumn(columnName: "contributor", tableName: "devops_app_share")
        dropColumn(columnName: "description", tableName: "devops_app_share")
        dropColumn(columnName: "img_url", tableName: "devops_app_share")
        dropColumn(columnName: "is_site", tableName: "devops_app_share")
        dropColumn(columnName: "is_free", tableName: "devops_app_share")
        renameColumn(columnDataType: 'VARCHAR(100)', newColumnName: 'share_level', oldColumnName: 'publish_level', remarks: '共享层级', tableName: 'devops_app_share')
        renameTable(newTableName: 'devops_app_share_rule', oldTableName: 'devops_app_share')
    }

    changeSet(id: '2019-07-26-modify-table', author: 'scp') {
        dropColumn(columnName: "category", tableName: "devops_app_share_rule")
        dropColumn(columnName: "is_active", tableName: "devops_app_share_rule")
        addColumn(tableName: 'devops_app_share_rule') {
            column(name: 'version_type', type: 'VARCHAR(50)', remarks: '版本类型',afterColumn: "share_level")
            column(name: 'version', type: 'VARCHAR(100)', remarks: '指定版本', afterColumn: "version_type")
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: 'project Id', afterColumn: "version")
            column(name: 'organization_id', type: 'BIGINT UNSIGNED', remarks: '组织Id', afterColumn: "project_id")
        }
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_app_share_rule')
    }

    changeSet(id: '2019-07-26-drop-column', author: 'scp') {
        dropColumn(columnName: "organization_id", tableName: "devops_app_share_rule")
    }

    changeSet(id: '2019-07-26-drop-constraint', author: 'scp') {
        dropUniqueConstraint(constraintName: "uk_app_id",tableName: "devops_app_share_rule")
    }

    changeSet(id: '2019-08-05-rename-table', author: 'scp') {
        renameTable(newTableName: 'devops_app_service_share_rule', oldTableName: 'devops_app_share_rule')
    }

    changeSet(author: 'zmf', id: '2019-08-06-rename-project-id-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_id', oldColumnName: 'project_id', tableName: 'devops_app_service_share_rule', remarks: '应用ID')
    }

    changeSet(author: 'li jinyan', id: '2019-08-14-drop-column'){
        dropColumn(columnName: "app_id", tableName: "devops_app_service_share_rule")
    }

    changeSet(author: 'scp', id: '2019-08-19-add-column') {
        addColumn(tableName: 'devops_app_service_share_rule') {
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '共享到特定项目', afterColumn: "version")
        }
    }
}