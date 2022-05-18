//package script.db.groovy.devops_service
//
//databaseChangeLog(logicalFilePath: 'dba/devops_app_exception_record.groovy') {
//    changeSet(author: 'wanghao', id: '2022-05-10-create-table') {
//        createTable(tableName: "devops_app_exception_record", remarks: 'chart应用异常信息记录表') {
//            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
//                constraints(primaryKey: true)
//            }
//            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
//                constraints(nullable: false)
//            }
//            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id') {
//                constraints(nullable: false)
//            }
//            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id') {
//                constraints(nullable: false)
//            }
//            column(name: 'resource_type', type: 'VARCHAR(32)', remarks: '资源类型') {
//                constraints(nullable: false)
//            }
//            column(name: 'resource_name', type: 'VARCHAR(128)', remarks: '资源名称') {
//                constraints(nullable: false)
//            }
//            column(name: "start_date", type: "DATETIME", remarks: '异常开始时间') {
//                constraints(nullable: false)
//            }
//            column(name: "end_date", type: "DATETIME", remarks: '异常结束时间')
//
//            column(name: 'downtime', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: '是否是停机状态') {
//                constraints(nullable: false)
//            }
//
//            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
//            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
//            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
//            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
//            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
//        }
//    }
//    changeSet(author: 'wanghao', id: '2022-05-10-add-index-env-id') {
//        createIndex(indexName: "idx_env_id", tableName: "devops_app_exception_record") {
//            column(name: "env_id")
//        }
//    }
//    changeSet(author: 'wanghao', id: '2022-05-10-add-index-app-id') {
//        createIndex(indexName: "idx_app_id", tableName: "devops_app_exception_record") {
//            column(name: "app_id")
//        }
//    }
//    changeSet(author: 'wanghao', id: '2022-05-10-add-index-project-id') {
//        createIndex(indexName: "idx_project_id", tableName: "devops_app_exception_record") {
//            column(name: "project_id")
//        }
//    }
//
//}
