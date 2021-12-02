package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_language.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_language') {
        createTable(tableName: "devops_ci_template_language", remarks: '流水线模板适用语言表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'language', type: 'VARCHAR(64)', remarks: '语言') {
                constraints(nullable: false)
            }
            column(name: 'source_type', type: 'VARCHAR(12)', remarks: '层级') {
                constraints(nullable: false)
            }

            column(name: 'source_id',  type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }

            column(name: 'built_in',  type: 'TINYINT UNSIGNED', remarks: '是否预置，1:预置，0:自定义') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_template_language', constraintName: 'uk_language', columnNames: 'language')

    }
}