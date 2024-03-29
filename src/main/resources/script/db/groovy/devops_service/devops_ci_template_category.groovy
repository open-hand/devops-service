package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_category.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_category') {
        createTable(tableName: "devops_ci_template_category", remarks: '流水线分类') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'category', type: 'VARCHAR(64)', remarks: '流水线分类') {
                constraints(nullable: false)
            }

            column(name: 'image', type: 'LONGTEXT', remarks: '分类的图表：base64格式')


            column(name: 'built_in', type: 'TINYINT UNSIGNED', remarks: '是否预置，1:预置，0:自定义') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }

    }
}