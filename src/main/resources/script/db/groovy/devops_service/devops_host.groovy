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

    changeSet(author: 'zmf', id: '2020-09-16-add-error-message') {
        addColumn(tableName: 'devops_host') {
            column(name: 'host_check_error', type: 'VARCHAR(1024)', remarks: '主机连接错误信息', afterColumn: 'host_status') {
                constraints(nullable: true)
            }
            column(name: 'jmeter_check_error', type: 'VARCHAR(1024)', remarks: 'jmeter连接错误信息', afterColumn: 'jmeter_status') {
                constraints(nullable: true)
            }
        }
    }

    changeSet(author: 'lihao', id: '2021-04-02-add-column') {
        addColumn(tableName: 'devops_host') {
            column(name: 'private_ip', type: 'VARCHAR(15)', remarks: '内网ip', afterColumn: 'host_ip') {
                constraints(nullable: true)
            }
            column(name: 'private_port', type: 'SMALLINT UNSIGNED', remarks: '内网ssh端口', afterColumn: 'ssh_port') {
                constraints(nullable: true)
            }
        }
    }
    changeSet(author: 'wanghao', id: '2021-06-25-modify-column') {

        dropUniqueConstraint(constraintName: "uk_project_ip_jmeter_port", tableName: "devops_host")
        dropColumn(columnName: "type", tableName: "devops_host")
        dropColumn(columnName: "jmeter_status", tableName: "devops_host")
        dropColumn(columnName: "jmeter_port", tableName: "devops_host")
        dropColumn(columnName: "jmeter_path", tableName: "devops_host")
        dropColumn(columnName: "jmeter_check_error", tableName: "devops_host")
        dropColumn(columnName: "private_ip", tableName: "devops_host")
        dropColumn(columnName: "private_port", tableName: "devops_host")
        addColumn(tableName: 'devops_host') {
            column(name: 'token', type: 'VARCHAR(64)', remarks: '主机token', afterColumn: 'host_status') {
                constraints(nullable: false)
            }
        }
    }

    changeSet(author: 'wanghao', id: '2021-07-02-drop-constraint') {
        dropNotNullConstraint(columnName: "username", columnDataType: "VARCHAR(32)", tableName: "devops_host")
        dropNotNullConstraint(columnName: "password", columnDataType: "VARCHAR(2048)", tableName: "devops_host")
        dropNotNullConstraint(columnName: "auth_type", columnDataType: "VARCHAR(63)", tableName: "devops_host")
    }

    changeSet(author: 'shanyu', id: '2021-07-16-drop-constraint') {
        dropNotNullConstraint(columnName: "host_ip", columnDataType: "VARCHAR(15)", tableName: "devops_host")
        dropNotNullConstraint(columnName: "ssh_port", columnDataType: "SMALLINT UNSIGNED", tableName: "devops_host")
        dropUniqueConstraint(constraintName: "uk_project_ip_port", tableName: "devops_host")
    }

    changeSet(author: 'lihao', id: '2021-07-29-add-is-skip-check-permission') {
        addColumn(tableName: 'devops_host') {
            column(name: 'skip_check_permission', type: 'TINYINT UNSIGNED', defaultValue: '1', remarks: '是否跳过环境权限校验 0 false 1 true')
        }
    }

    changeSet(author: 'lihao', id: '2021-10-13-drop-is-skip-check-permission') {
        dropColumn(columnName: "skip_check_permission", tableName: "devops_host")
    }

    changeSet(author: 'lihao', id: '2022-03-17-modify-column') {
        modifyDataType(tableName: 'devops_host', columnName: 'password', newDataType: 'TEXT')
    }

    changeSet(author: 'lihao', id: '2022-08-17-add-description') {
        addColumn(tableName: 'devops_host') {
            column(name: 'description', type: 'VARCHAR(100)', remarks: '主机描述', afterColumn: 'name')
        }
    }

    changeSet(author: 'lihao', id: '2023-01-05-add-network') {
        addColumn(tableName: 'devops_host') {
            column(name: 'network', type: 'TEXT', remarks: '网卡信息', afterColumn: 'description')
        }
    }
}