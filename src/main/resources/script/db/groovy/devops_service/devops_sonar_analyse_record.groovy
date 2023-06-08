package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_sonar_analyse_record.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_sonar_analyse_record", remarks: '代码扫描记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: "project_id", type: "BIGINT UNSIGNED", remarks: "项目Id") {
                constraints(nullable: false)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id') {
                constraints(nullable: false)
            }
            column(name: 'commit_sha', type: 'VARCHAR(128)', remarks: 'commit sha')
            column(name: "analysed_at", type: "DATETIME", remarks: '扫描时间') {
                constraints(nullable: false)
            }
            column(name: 'bug', type: 'BIGINT UNSIGNED', remarks: 'bug数', defaultValue: "0")
            column(name: 'code_smell', type: 'BIGINT UNSIGNED', remarks: '代码异味数', defaultValue: "0")
            column(name: 'vulnerability', type: 'BIGINT UNSIGNED', remarks: '漏洞数', defaultValue: "0")
            column(name: 'sqale_index', type: 'BIGINT UNSIGNED', remarks: '技术债务', defaultValue: "0")
            column(name: 'quality_gate_details', type: 'VARCHAR(4000)', remarks: '质量门详情')
            column(name: 'score', type: 'BIGINT UNSIGNED', remarks: '评分')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")

            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_sonar_analyse_record', indexName: 'devops_sonar_analyse_record_n1') {
            column(name: 'project_id')
        }
        createIndex(tableName: 'devops_sonar_analyse_record', indexName: 'devops_sonar_analyse_record_n2') {
            column(name: 'app_service_id')
        }
        createIndex(tableName: 'devops_sonar_analyse_record', indexName: 'devops_sonar_analyse_record_n3') {
            column(name: 'commit_sha')
        }

    }
}
