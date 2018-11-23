package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_template.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_app_template", remarks: '应用模板') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'organization_id', type: 'BIGINT UNSIGNED', remarks: '组织 ID')
            column(name: 'gitlab_project_id', type: 'BIGINT UNSIGNED', remarks: 'GitLab 项目 ID')
            column(name: 'name', type: 'VARCHAR(32)', remarks: '模板名称')
            column(name: 'code', type: 'VARCHAR(32)', remarks: '模板编码')
            column(name: 'type', type: 'TINYINT UNSIGNED', remarks: '模板类型')
            column(name: 'copy_from', type: 'BIGINT UNSIGNED', remarks: '复制于')
            column(name: 'repo_url', type: 'VARCHAR(128)', remarks: '模板地址')
            column(name: 'uuid', type: 'VARCHAR(50)')
            column(name: 'description', type: 'VARCHAR(128)', remarks: '模板描述')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_organization_id ", tableName: "devops_app_template") {
            column(name: "organization_id")
        }
        addUniqueConstraint(tableName: 'devops_app_template',
                constraintName: 'uk_org_id_name', columnNames: 'organization_id,name')
        addUniqueConstraint(tableName: 'devops_app_template',
                constraintName: 'uk_org_id_code', columnNames: 'organization_id,code')
    }

    changeSet(author: 'n1ck', id: '2018-11-20-modify-column-collate') {
        sql("ALTER TABLE devops_app_template MODIFY COLUMN `name` VARCHAR(32) BINARY")
    }

    changeSet(author: 'younger', id: '2018-11-21-add-column') {
        addColumn(tableName: 'devops_app_template') {
            column(name: 'is_synchro', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: 'is synchro', afterColumn: 'gitlab_project_id')
            column(name: 'is_failed', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: 'is failed', afterColumn: 'is_synchro')
        }
        sql("UPDATE  devops_app_template  dat SET dat.is_synchro= (CASE when dat.gitlab_project_id is not null THEN 1  else  0  END)")
        sql("UPDATE devops_app_template  dat SET dat.is_failed= (CASE when dat.gitlab_project_id  is  null THEN 1  else  0  END)")
    }

    changeSet(author: 'younger', id: '2018-11-23-add-sql') {
        sql("UPDATE devops_app_template  dat SET dat.is_failed= 0,dat.is_synchro= 1 where organization_id is null")
    }
}