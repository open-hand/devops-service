package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_docker_instance.groovy') {
    changeSet(author: 'scp', id: '2021-06-30-create-table') {
        createTable(tableName: "devops_docker_instance", remarks: 'Docker实例') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: "host_id", type: "BIGINT UNSIGNED", remarks: '主机id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(128)', remarks: '容器名') {
                constraints(nullable: false)
            }
            column(name: 'container_id', type: 'VARCHAR(128)', remarks: '容器id')
            column(name: 'image', type: 'VARCHAR(256)', remarks: '镜像地址') {
                constraints(nullable: false)
            }

            column(name: 'ports', type: 'VARCHAR(256)', remarks: '端口映射列表')

            column(name: 'status', type: 'VARCHAR(20)', remarks: '容器状态')
            column(name: 'source_type', type: 'VARCHAR(20)', remarks: '部署来源') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_docker_instance',
                constraintName: 'uk_host_id_name', columnNames: 'host_id,name')
    }

    changeSet(author: 'wx', id: '2022-02-18-add-app-id') {
        addColumn(tableName: 'devops_docker_instance') {
            column(name: 'app_id', type: "BIGINT UNSIGNED", defaultValue: '0', remarks: '主机应用id', afterColumn: 'container_id') {
                constraints(nullable: false)
            }
            column(name: 'repo_type', type: 'VARCHAR(256)', remarks: '镜像仓库的类型，默认的还是自定义的', afterColumn: 'container_id') {
            }
            column(name: 'repo_name', type: 'VARCHAR(256)', remarks: '仓库的名称，默认的才有', afterColumn: 'container_id') {
            }
            column(name: 'repo_id', type: 'VARCHAR(256)', remarks: '仓库的id,默认的才有', afterColumn: 'container_id') {
            }
            column(name: 'user_name', type: 'VARCHAR(256)', remarks: '自定义仓库的用户名', afterColumn: 'container_id') {
            }
            column(name: 'pass_word', type: 'VARCHAR(256)', remarks: '自定义仓库的密码', afterColumn: 'container_id') {
            }
            column(name: 'private_repository', type: 'TINYINT UNSIGNED', remarks: '自定义仓库是否共享', afterColumn: 'container_id') {
            }
            column(name: 'image_name', type: 'VARCHAR(256)', remarks: '默认仓库的镜像名称', afterColumn: 'container_id') {
            }
            column(name: 'tag', type: 'VARCHAR(256)', remarks: '默认仓库的镜像版本', afterColumn: 'container_id') {
            }
            column(name: 'docker_command', type: 'TEXT', remarks: '命令框的命令', afterColumn: 'container_id') {
            }

        }
    }

}