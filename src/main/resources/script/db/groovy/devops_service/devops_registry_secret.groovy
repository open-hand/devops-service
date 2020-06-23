package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_registry_secret.groovy') {
    changeSet(author: 'Sheep', id: '2019-03-14-create-table') {
        createTable(tableName: "devops_registry_secret", remarks: '私有镜像仓库密钥') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id')
            column(name: 'namespace', type: 'VARCHAR(32)', remarks: '命名空间')
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '配置Id')
            column(name: 'secret_code', type: 'VARCHAR(32)', remarks: 'secret编码')
            column(name: 'secret_detail', type: 'VARCHAR(1000)', remarks: 'secret内容')
            column(name: 'status', type: 'VARCHAR(32)', remarks: 'secret状态')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: "2020-02-12-registry-secret-add-cluster-id", author: "zmf") {
        addColumn(tableName: 'devops_registry_secret') {
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '集群id', afterColumn: "namespace")
        }

        preConditions (onFail: 'MARK_RAN') {
            tableExists(tableName: "devops_env")
        }
        // 这个sql会更新cluster_id字段的值，但是有些脏数据的env_id已经没有cluster_id了
        // 所有有些数据的cluster_id是空的，这些数据一般是无用的，之后视情况删除
        sql("""
            UPDATE devops_registry_secret drs
            SET drs.cluster_id = (SELECT cluster_id FROM devops_env de WHERE de.id = drs.env_id)
        """)
    }

    changeSet(id: "2020-02-24-registry-secret-add-project-id", author: "scp") {
        addColumn(tableName: 'devops_registry_secret') {
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id', afterColumn: "cluster_id")
        }

        preConditions (onFail: 'MARK_RAN') {
            tableExists(tableName: "devops_env")
        }
        sql("""
            UPDATE devops_registry_secret drs
            SET drs.project_id = (SELECT project_id FROM devops_env de WHERE de.id = drs.env_id)
        """)
    }

    changeSet(id: '2020-03-04-registry-secret-add-unique-constraint', author: 'zmf', failOnError: false) {
        addUniqueConstraint(tableName: 'devops_registry_secret',
                constraintName: 'registry_secret_uk_config_cluster_namespace', columnNames: 'config_id,cluster_id,namespace,project_id')
    }
    changeSet(author: 'wx', id: '2020-6-16-add-column') {
        addColumn(tableName: 'devops_registry_secret') {
            column(name: 'repo_type', type: 'VARCHAR(64)', afterColumn: 'id', remarks: '仓库类型(DEFAULT_REPO、CUSTOM_REPO)')
        }
    }
    changeSet(author: 'wx', id: '2020-6-23-update-column') {
        sql("""
              alter table 
              devops_registry_secret 
              modify secret_detail varchar(1500)
            """)
    }
}
