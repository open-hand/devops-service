package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_jenkins_server.groovy') {
    changeSet(author: 'lihao', id: '2023-02-28-create-table') {
        createTable(tableName: "devops_jenkins_server", remarks: 'jenkins服务器配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(100)', remarks: '名称')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id')
            column(name: 'url', type: 'VARCHAR(1024)', remarks: 'URL')
            column(name: 'username', type: 'VARCHAR(48)', remarks: '用户名')
            column(name: 'password', type: 'VARCHAR(100)', remarks: '密码')
            column(name: 'description', type: 'VARCHAR(500)', remarks: '描述')
            column(name: 'status', type: 'VARCHAR(30)', remarks: '连接状态')


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_jenkins_server',
                constraintName: 'devops_jenkins_server_u1', columnNames: 'project_id,name')
    }
}