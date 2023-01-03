package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_template.groovy') {
    changeSet(author: 'scp', id: '2021-03-09-create-table') {
        createTable(tableName: "devops_app_template", remarks: '应用模板') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'source_id', type: 'BIGINT UNSIGNED', remarks: '组织ID/projectId/平台层Id=0')
            column(name: 'source_type', type: 'VARCHAR(20)', remarks: 'tenant/project/site')
            column(name: 'gitlab_project_id', type: 'BIGINT UNSIGNED', remarks: 'GitLab 项目 ID')
            column(name: 'gitlab_url', type: 'VARCHAR(256)', remarks: '模板gitlab地址')
            column(name: 'name', type: 'VARCHAR(40)', remarks: '模板名称')
            column(name: 'code', type: 'VARCHAR(30)', remarks: '模板编码')
            column(name: 'type', type: 'VARCHAR(2)', remarks: '模板类型,P:预定义，C:自定义')
            column(name: 'enable', type: 'TINYINT UNSIGNED', defaultValue: "1",remarks: '是否启用状态')
            column(name: 'status', type: 'VARCHAR(2)',remarks: 'S成功/F：失败/C:创建中')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_app_template',
                constraintName: 'uk_source_id_name', columnNames: 'source_id,source_type,name')
        addUniqueConstraint(tableName: 'devops_app_template',
                constraintName: 'uk_source_id_code', columnNames: 'source_id,source_type,code')
    }
    changeSet(author: 'scp', id: '2021-04-07-add-gitlab-project-unique-index') {
        addUniqueConstraint(tableName: "devops_app_template", constraintName: 'uk_app_gitlab_project_id', columnNames: "gitlab_project_id")
    }

    changeSet(author: 'scp', id: '2021-05-21-devops_app_template-add-remark') {
        addColumn(tableName: 'devops_app_template') {
            column(name: 'remark', type: 'text', remarks: '备注', beforeColumn: "object_version_number")
        }
    }
    changeSet(author: 'wanghao', id: '2023-01-03-delete-data') {
        sql("""
        DELETE FROM devops_app_template WHERE source_type = 'site' and code in ('mochatemplate', 'testngseleniumtemplate','testngtemplate')
        """)
    }
}