package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cluster_node.groovy') {
    changeSet(author: 'lihao', id: '2020-10-19-create-table') {
        createTable(tableName: 'devops_cluster_node', remarks: '集群节点') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(128)', remarks: '节点名称') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(10)', remarks: '连接类型，作为连接介质节点或者集群节点') {
                constraints(nullable: false)
            }
            column(name: 'role', type: 'SMALLINT UNSIGNED', remarks: '节点拥有的角色')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '集群id') {
                constraints(nullable: false)
            }
            column(name: 'host_ip', type: 'VARCHAR(15)', remarks: '节点ip') {
                constraints(nullable: false)
            }
            column(name: 'host_port', type: 'SMALLINT UNSIGNED', remarks: '节点ssh的端口') {
                constraints(nullable: false)
            }
            column(name: 'auth_type', type: 'VARCHAR(63)', remarks: '认证类型') {
                constraints(nullable: false)
            }
            column(name: 'username', type: 'VARCHAR(32)', remarks: '用户名') {
                constraints(nullable: false)
            }
            column(name: 'password', type: 'text', remarks: '密码/rsa秘钥') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_cluster_node',
                constraintName: 'uk_cluster_host_name', columnNames: 'cluster_id,name')
        addUniqueConstraint(tableName: 'devops_cluster_node',
                constraintName: 'uk_project_ip_port', columnNames: 'project_id,host_ip,host_port')
    }
    changeSet(author: 'lihao', id: '2020-11-12-add-column') {
        addColumn(tableName: 'devops_cluster_node') {
            column(name: "inner_node_name", type: "VARCHAR(128)", remarks: '既作为外部节点，又作为内部节点，这个字段表示作为的内部节点的名称', afterColumn: 'name')
        }
    }
}