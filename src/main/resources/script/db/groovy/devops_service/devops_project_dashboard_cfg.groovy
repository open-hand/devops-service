package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_project_dashboard_cfg.groovy') {
    changeSet(author: 'wanghao', id: '2023-05-31-create-table') {
        createTable(tableName: "devops_project_dashboard_cfg", remarks: '项目质量评分配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: "tenant_id", type: "BIGINT UNSIGNED", remarks: "租户Id") {
                constraints(nullable: false)
            }
            column(name: 'pass_score', type: 'FLOAT', remarks: '合格分数') {
                constraints(nullable: false)
            }
            column(name: "code_weight", type: "BIGINT UNSIGNED", remarks: '代码权重') {
                constraints(nullable: false)
            }
            column(name: "vuln_weight", type: "BIGINT UNSIGNED", remarks: '漏洞权重') {
                constraints(nullable: false)
            }
            column(name: "k8s_weight", type: "BIGINT UNSIGNED", remarks: 'k8s权重') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")

            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_project_dashboard_cfg',
                constraintName: 'devops_project_dashboard_cfg_u1', columnNames: 'tenant_id')

    }
}
