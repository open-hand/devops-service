package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_user_rel.groovy') {
    changeSet(author: 'n1ck', id: '2018-11-22-create-table') {
        createTable(tableName: "devops_app_user_rel", remarks: '应用用户权限表') {
            column(name: 'iam_user_id', type: 'BIGINT UNSIGNED', remarks: '用户id')
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_app_user_rel',
                constraintName: 'uk_iam_user_id_app_id', columnNames: 'iam_user_id,app_id')

        createIndex(indexName: "idx_app_id", tableName: "devops_app_user_rel") {
            column(name: "app_id")
        }
    }
}