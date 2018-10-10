package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_user.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_user", remarks: 'DevOps 用户表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，Iam用户ID') {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_user_id', type: 'BIGINT UNSIGNED', remarks: 'Gitlab用户ID') {
                constraints(unique: true)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


    changeSet(id: '2018-10-08-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'iam_user_id', oldColumnName: 'id', remarks: 'iam user id', tableName: 'devops_user')
    }

    changeSet(id: '2018-10-08-init-column', author: 'younger') {

        preConditions(onFail: 'MARK_RAN'){
            sqlCheck(expectedResult: 0,sql:"select count(*) from devops_user where iam_user_id=1")
        }
        sql("insert into devops_user  (iam_user_id,gitlab_user_id)  values (1,1)")
    }

}