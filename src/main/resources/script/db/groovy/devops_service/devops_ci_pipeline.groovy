package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_pipeline", remarks: 'devops_ci_pipeline') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '流水线名称')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id')
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id')
            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发方式:auto, 自动触发，manual')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline',
                constraintName: 'uk_app_service_id', columnNames: 'app_service_id')
    }
    changeSet(author: 'wanghao', id: '2020-04-07-add-column') {
        addColumn(tableName: 'devops_ci_pipeline') {
            column(name: "is_enabled", type: "TINYINT UNSIGNED", defaultValue: "1", remarks: '是否启用')
        }
    }
    changeSet(author: 'zmf', id: '2020-04-20-add-token') {
        addColumn(tableName: 'devops_ci_pipeline') {
            column(name: 'token', type: 'CHAR(36)', remarks: '流水线token，安全性考虑')
        }
    }

    changeSet(author: 'zmf', id: '2020-04-28-pipeline-add-image') {
        addColumn(tableName: 'devops_ci_pipeline') {
            column(name: 'image', type: 'VARCHAR(280)', remarks: '流水线的镜像地址') {
                constraints(nullable: true)
            }
        }
    }
    changeSet(author: 'wx', id: '2020-04-28-update-devops_cicd_pipeline'){
        renameTable(newTableName: 'devops_cicd_pipeline', oldTableName: 'devops_ci_pipeline')

    }
    changeSet(author: 'scp', id: '2020-08-28-add-version-name'){
        addColumn(tableName: 'devops_cicd_pipeline') {
            column(name: 'version_name', type: 'VARCHAR(100)', remarks: '自定义版本名称')
        }
    }
    changeSet(author: 'wanghao', id: '2020-11-11-add-index') {
        createIndex(indexName: "idx_project_id", tableName: "devops_cicd_pipeline") {
            column(name: "project_id")
        }
    }

    changeSet(author: 'wx', id: '2021-06-24-fix-data-pipeline') {
        sql("""
            UPDATE devops_cicd_pipeline dcp 
            SET dcp.image="registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.4"
            WHERE dcp.image LIKE 'registry.cn-shanghai.aliyuncs.com/c7n/cibase%';
        """)
    }
    changeSet(author: 'wanghao', id: '2021-12-14-drop-column') {
        dropColumn(columnName: "trigger_type", tableName: "devops_cicd_pipeline")
    }

    changeSet(author: 'wanghao', id: '2023-12-14-add-column') {
        addColumn(tableName: 'devops_cicd_pipeline') {
            column(name: 'is_interruptible', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: '是否可中断', afterColumn: 'is_enabled')
        }
    }

}
