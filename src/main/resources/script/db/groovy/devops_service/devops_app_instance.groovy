package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_instance.groovy') {
    changeSet(author: 'Zenger', id: '2018-04-12-create-table') {
        createTable(tableName: "devops_app_instance", remarks: '应用实例表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'code', type: 'VARCHAR(64)', remarks: '实例code')
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用 ID')
            column(name: 'app_version_id', type: 'BIGINT UNSIGNED', remarks: '应用版本 ID')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '实例状态')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_app_instance',
                constraintName: 'uk_code', columnNames: 'code')

        createIndex(indexName: "idx_status ", tableName: "devops_app_instance") {
            column(name: "status")
        }
    }


    changeSet(author: 'younger', id: '2018-09-10-add-column') {
        addColumn(tableName: 'devops_app_instance') {
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'command id', afterColumn: 'env_id')
        }
    }

    changeSet(author: 'scp', id: '2019-06-04-idx-app-id') {
        createIndex(indexName: "idx_app_id ", tableName: "devops_app_instance") {
            column(name: "app_id")
        }
    }

    changeSet(author: 'scp', id: '2019-06-05-idx-env-id') {
        createIndex(indexName: "idx_env_id ", tableName: "devops_app_instance") {
            column(name: "env_id")
        }
    }

    changeSet(author: 'younger', id: '2019-06-05-add-column') {
        addColumn(tableName: 'devops_app_instance') {
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: 'devops deploy value id', afterColumn: 'env_id')
        }
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_app_instance')
    }

    changeSet(author: 'sheep', id: '2019-8-02-rename-table') {
        renameTable(newTableName: 'devops_app_service_instance', oldTableName: 'devops_app_instance')
    }

    changeSet(author: 'zmf', id: '2019-08-06-rename-app-version-id-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_version_id', oldColumnName: 'app_version_id', tableName: 'devops_app_service_instance', remarks: '应用版本 ID')
    }

    changeSet(id: '2019-08-26-drop-constraint', author: 'younger') {
        dropUniqueConstraint(tableName: 'devops_app_service_instance',
                constraintName: 'uk_code')

    }

    changeSet(id: '2019-11-04-add-component-name-version', author: 'zmf') {
        addColumn(tableName: 'devops_app_service_instance') {
            column(name: 'component_version', type: 'VARCHAR(64)', remarks: '组件对应实例的版本号', afterColumn: 'status')
            column(name: 'component_chart_name', type: 'VARCHAR(64)', remarks: '组件对应实例的chart名称', afterColumn: 'status')
        }
    }

    changeSet(id: '2019-11-12-add-effect-command-id', author: 'zmf') {
        addColumn(tableName: 'devops_app_service_instance') {
            column(name: 'effect_command_id', type: 'BIGINT UNSIGNED', remarks: '当前实例生效的command id', afterColumn: 'command_id')
        }
        sql("""
            UPDATE 
            devops_app_service_instance dasi 
            SET dasi.effect_command_id = dasi.command_id
            WHERE dasi.effect_command_id IS NULL
            """)
    }

    changeSet(id: '2020-12-14-add-market-columns', author: 'zmf') {
        addColumn(tableName: 'devops_app_service_instance') {
            column(name: 'source', type: 'VARCHAR(32)', remarks: '实例的部署来源/normal/market', afterColumn: 'app_service_id', defaultValue: 'normal') {
                constraints(nullable: false)
            }
        }
    }

    changeSet(id: '2022-11-17-add-column',author: 'lihao'){
        addColumn(tableName:'devops_app_service_instance' ){
            column(name: 'replicas_strategy',type: 'VARCHAR(16)',remarks: '副本数生效策略 values/deployment',afterColumn: 'source')
        }
    }
}