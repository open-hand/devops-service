package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_hzero_deploy_details.groovy') {
    changeSet(author: 'wanghao', id: '2021-07-27-create-table') {
        createTable(tableName: "devops_hzero_deploy_details", remarks: 'hzero部署记录明细表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'deploy_record_id', type: 'BIGINT UNSIGNED', remarks: 'hzero部署记录id') {
                constraints(nullable: false)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id') {
                constraints(nullable: false)
            }
            column(name: 'mkt_service_id', type: 'BIGINT UNSIGNED', remarks: '市场服务id') {
                constraints(nullable: false)
            }
            column(name: 'mkt_deploy_object_id', type: 'BIGINT UNSIGNED', remarks: '部署对象id') {
                constraints(nullable: false)
            }
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: '部署配置id') {
                constraints(nullable: false)
            }
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'gitops操作记录 id')

            column(name: 'status', type: 'VARCHAR(32)', remarks: '部署状态') {
                constraints(nullable: false)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id')

            column(name: 'app_name', type: 'VARCHAR(255)', remarks: '应用名称') {
                constraints(nullable: false)
            }
            column(name: 'app_code', type: 'VARCHAR(255)', remarks: '应用code') {
                constraints(nullable: false)
            }
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '部署顺序') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_project_id", tableName: "devops_hzero_deploy_details") {
            column(name: "deploy_record_id")
        }
        createIndex(indexName: "idx_env_id", tableName: "devops_hzero_deploy_details") {
            column(name: "env_id")
        }

    }

    changeSet(author: 'sy', id: '2021-08-17-add-column') {
        addColumn(tableName: 'devops_hzero_deploy_details') {
            column(name: 'start_time', type: 'DATETIME', remarks: '开始部署时间', afterColumn: 'sequence')
        }
        addColumn(tableName: 'devops_hzero_deploy_details') {
            column(name: 'end_time', type: 'DATETIME', remarks: '结束部署时间', afterColumn: 'sequence')
        }
    }

    changeSet(author: 'wanghao', id: '2021-11-05-add-column') {
        addColumn(tableName: 'devops_hzero_deploy_details') {
            column(name: 'hzero_type', type: 'VARCHAR(32)', remarks: 'hzero版本：sass、open', afterColumn: 'sequence')
        }
    }

    changeSet(author: 'wanghao',id: '2021-11-26-add-index'){
        createIndex(indexName: "idx_app_id", tableName: "devops_hzero_deploy_details") {
            column(name: "app_id")
        }
    }
}