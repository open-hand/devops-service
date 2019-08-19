package script.db

databaseChangeLog(logicalFilePath: 'db/devops_certification_file.groovy') {
    changeSet(author: 'zhanglei', id: '2018-9-11-create-table') {
        createTable(tableName: "devops_certification_file", remarks: 'C7N Certification file') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'cert_id', type: 'BIGINT UNSIGNED', remarks: 'certificate id')
            column(name: 'key_file', type: 'TEXT', remarks: 'key file content')
            column(name: 'cert_file', type: 'TEXT', remarks: 'cert file content')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_certification_file', constraintName: 'uk_cert_id', columnNames: 'cert_id')
    }

    changeSet(author: 'Younger', id: '2018-10-08-move-data') {
        preConditions{
            columnExists(tableName: "devops_certification_file",columnName:"cert_id")
        }
        sql("update devops_certification A,devops_certification_file B set A.certification_file_id=B.id where A.id=B.cert_id")
    }

    changeSet(author: 'Younger', id: '2018-10-08-drop-column') {
        dropColumn(columnName: "cert_id", tableName: "devops_certification_file")

    }

}