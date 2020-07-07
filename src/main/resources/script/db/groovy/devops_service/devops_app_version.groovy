package script.db.groovy.devops_service

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

    changeSet(author: 'scp', id: '2019-04-16-app-version-add--index') {
        createIndex(indexName: "idx_commit_version", tableName: "devops_app_version") {
            column(name: "commit")
            column(name: 'version')
        }
    }

    changeSet(author: 'scp', id: '2019-07-03-add-column') {
        addColumn(tableName: 'devops_app_version') {
            column(name: 'publish_time', type: 'DATETIME', remarks: 'publish time', afterColumn: 'is_publish')
        }
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_app_version')
    }


    changeSet(author: 'sheep', id: '2019-8-05-rename-table') {
        renameTable(newTableName: 'devops_app_service_version', oldTableName: 'devops_app_version')
    }

    changeSet(author: 'scp', id: '2019-10-16-add-drop-column') {
        addColumn(tableName: 'devops_app_service_version') {
            column(name: 'harbor_config_id', type: 'BIGINT UNSIGNED', afterColumn: 'id', remarks: '配置Id')
            column(name: 'helm_config_id', type: 'BIGINT UNSIGNED', afterColumn: 'id', remarks: '配置Id')
        }
        dropColumn(columnName: "is_publish", tableName: "devops_app_service_version")
        dropColumn(columnName: "publish_time", tableName: "devops_app_service_version")
    }

    changeSet(author: 'wx', id: '2020-6-15-add-column') {
        addColumn(tableName: 'devops_app_service_version') {
            column(name: 'repo_type', type: 'VARCHAR(64)', afterColumn: 'id', remarks: '仓库类型(DEFAULT_REPO、CUSTOM_REPO)')
        }
        sql("""
            UPDATE 
            devops_app_service_version dasv 
            set object_version_number=1
            """)
    }
    changeSet(author: 'wanghao', id: '2020-7-07-add-column') {
        addColumn(tableName: 'devops_app_service_version') {
            column(name: 'ref', type: 'VARCHAR(64)', afterColumn: 'commit', remarks: 'gitlab commit ref')
        }
    }
}