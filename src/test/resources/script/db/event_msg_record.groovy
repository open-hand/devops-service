package script.db

databaseChangeLog(logicalFilePath: 'event_consumer_record.groovy') {
    changeSet(id: '2018-02-06-add-table-event_consumer_record', author: 'flyleft') {
        createTable(tableName: "event_consumer_record") {
            column(name: 'uuid', type: 'VARCHAR(50)', autoIncrement: false, remarks: 'uuid') {
                constraints(primaryKey: true)
            }
            column(name: 'create_time', type: 'DATETIME', remarks: '创建时间')
        }
    }


    changeSet(author: 'Younger', id: '2018-10-08-delete-table') {
        dropTable(tableName: "event_consumer_record")
    }
}