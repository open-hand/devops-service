package script.db


databaseChangeLog(logicalFilePath: 'dba/devops_pv_project_rel.groovy') {
    changeSet(author: 'yzj', id: '2019-11-1-create-table') {
        createTable(tableName: "devops_pv_project_rel.groovy", remarks: 'pv和项目关联关系表') {
            column(name: 'pv_id', type: 'BIGINT UNSIGNED', remarks: '集群Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}