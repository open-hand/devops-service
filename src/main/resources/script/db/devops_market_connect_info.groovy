package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_market_connect_info.groovy') {
    changeSet(author: 'scp', id: '2019-06-28-create-table') {
        createTable(tableName: "devops_market_connect_info", remarks: '应用共享资源') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'saas_market_url', type: 'varchar(255)', remarks: '远程应用市场连接地址')
            column(name: 'access_token', type: 'varchar(128)', remarks: '连接远程应用市场的token')
        }
    }
}
