package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_docker.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_docker') {
        createTable(tableName: "devops_ci_template_docker", remarks: '流水线任务模板与步骤模板关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'docker_file_path', type: 'VARCHAR(255)', remarks: 'docker file 地址') {
                constraints(nullable: false)
            }
            column(name: 'docker_context_dir', type: 'VARCHAR(255)', remarks: 'docker 上下文路径') {
                constraints(nullable: false)
            }
            column(name: 'enable_docker_tls_verify', type: 'TINYINT UNSIGNED', defaultValue: "true", remarks: '是否启用tls') {
                constraints(nullable: false)
            }
            column(name: 'image_scan', type: 'TINYINT UNSIGNED', defaultValue: "false", remarks: '是否是否开启镜像扫描') {
                constraints(nullable: false)
            }
            column(name: 'security_control', type: 'TINYINT UNSIGNED', defaultValue: "false", remarks: '是否开启门禁检查') {
                constraints(nullable: false)
            }
            column(name: 'severity', type: 'VARCHAR(255)', remarks: '漏洞危险程度')
            column(name: 'security_control_conditions', type: 'VARCHAR(255)', remarks: '门禁条件')
            column(name: 'vulnerability_count', type: 'INT(4)', remarks: '漏洞数量')
            column(name: 'ci_template_step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤Id') {
                constraints(nullable: true)
            }


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_template_docker', constraintName: 'uk_ci_template_step_id', columnNames: 'ci_template_step_id')

    }
}