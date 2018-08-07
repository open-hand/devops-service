package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_env", remarks: '环境管理') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，环境ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目 ID')
            column(name: 'name', type: 'VARCHAR(32)', remarks: '环境名称')
            column(name: 'code', type: 'VARCHAR(32)', remarks: '环境编码')
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '序号')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: '命名空间')
            column(name: 'token', type: 'CHAR(36)', remarks: 'token')
            column(name: 'description', type: 'VARCHAR(64)', remarks: '环境描述')
            column(name: 'is_active', type: 'TINYINT UNSIGNED', remarks: '是否可用')
            column(name: 'is_connected', type: 'TINYINT UNSIGNED', remarks: '环境状态')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_name', columnNames: 'project_id,name')
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_code', columnNames: 'project_id,code')
        createIndex(indexName: "idx_project_id", tableName: "devops_env") {
            column(name: "project_id")
        }
    }

    changeSet(id: '2018-05-20-drop-constraint', author: 'younger') {
        dropUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_code')

    }

    changeSet(author: 'younger', id: '2018-05-21-drop-column')
            {
                dropColumn(columnName: "code", tableName: "devops_env")
            }
    changeSet(id: '2018-05-21-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'varchar(128)', newColumnName: 'code', oldColumnName: 'namespace', remarks: '环境命名空间', tableName: 'devops_env')
    }
    changeSet(id: '2018-05-21-add-constraint', author: 'younger') {
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_code', columnNames: 'project_id,code')

    }

    changeSet(author: 'younger', id: '2018-07-25-add-column')
            {
                addColumn(tableName: 'devops_env') {
                    column(name: 'gitlab_env_project_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab env project id', afterColumn: 'project_id')
                    column(name: 'env_id_rsa', type: 'varchar(5000)', remarks: 'ssh id rsa ', afterColumn: 'token')
                    column(name: 'env_id_rsa_pub', type: 'varchar(5000)', remarks: 'ssh id rsa pub', afterColumn: 'env_id_rsa')
                    column(name: 'hook_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab webhook', afterColumn: 'gitlab_env_project_id')
                }
            }
    changeSet(author: 'younger', id: '2018-08-1-add-column')
            {
                addColumn(tableName: 'devops_env') {
                    column(name: 'git_commit', type: 'BIGINT UNSIGNED', remarks: 'env_commit_id', afterColumn: 'hook_id')
                    column(name: 'devops_sync_commit', type: 'BIGINT UNSIGNED', remarks: 'env_commit_devops_sync_id', afterColumn: 'git_commit')
                    column(name: 'agent_sync_commit', type: 'BIGINT UNSIGNED', remarks: 'senv_commit_agent_sync_id', afterColumn: 'devops_sync_commit')
                }
            }
}