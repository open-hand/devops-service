package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_host.groovy') {
    changeSet(author: 'zmf', id: '2020-09-14-create-host_table') {
        createTable(tableName: "devops_host", remarks: '主机配置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(128)', remarks: '主机名称') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(63)', remarks: '主机类型') {
                constraints(nullable: false)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: 'host_status', type: 'VARCHAR(63)', remarks: '主机状态') {
                constraints(nullable: false)
            }
            column(name: 'jmeter_status', type: 'VARCHAR(63)', remarks: 'Jmeter状态') {
                constraints(nullable: true)
            }
            column(name: 'host_ip', type: 'VARCHAR(15)', remarks: '主机ip') {
                constraints(nullable: false)
            }
            column(name: 'ssh_port', type: 'SMALLINT UNSIGNED', remarks: '主机ssh的端口') {
                constraints(nullable: false)
            }
            column(name: 'auth_type', type: 'VARCHAR(63)', remarks: '认证类型') {
                constraints(nullable: false)
            }
            column(name: 'username', type: 'VARCHAR(32)', remarks: '用户名') {
                constraints(nullable: false)
            }
            column(name: 'password', type: 'VARCHAR(2048)', remarks: '密码/rsa秘钥') {
                constraints(nullable: false)
            }
            column(name: 'jmeter_port', type: 'SMALLINT UNSIGNED', remarks: 'jmeter进程的端口号') {
                constraints(nullable: true)
            }
            column(name: 'jmeter_path', type: 'VARCHAR(512)', remarks: 'jmeter二进制文件的路径') {
                constraints(nullable: true)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_host',
                constraintName: 'uk_project_host_name', columnNames: 'project_id,name')
        addUniqueConstraint(tableName: 'devops_host',
                constraintName: 'uk_project_ip_port', columnNames: 'project_id,host_ip,ssh_port')
        addUniqueConstraint(tableName: 'devops_host',
                constraintName: 'uk_project_ip_jmeter_port', columnNames: 'project_id,host_ip,jmeter_port')
    }
}