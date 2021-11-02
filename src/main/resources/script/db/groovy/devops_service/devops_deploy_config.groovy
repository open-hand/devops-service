package script.db
databaseChangeLog(logicalFilePath: 'script/db/devops_deploy_config.groovy') {
    changeSet(author: "jian.zhang02@hand-china.com", id: "devops_deploy_config-2021-08-19-version-1"){
        createTable(tableName: "devops_deploy_config", remarks: "主机部署文件配置表") {
            column(name: "id", type: "BIGINT(20) UNSIGNED",   remarks: "主键ID")  {constraints(primaryKey: true)} 
            column(name: "project_id", type: "BIGINT(20) UNSIGNED",   remarks: "项目ID")  {constraints(nullable:"false")}  
            column(name: "deploy_record_id", type: "BIGINT(20) UNSIGNED",   remarks: "部署记录ID")  {constraints(nullable:"false")}  
            column(name: "host_id", type: "BIGINT(20) UNSIGNED",   remarks: "主机ID")  {constraints(nullable:"false")}  
            column(name: "deploy_object_key", type: "VARCHAR(120)",  remarks: "部署对象")  {constraints(nullable:"false")}  
            column(name: "instance_name", type: "VARCHAR(128)",  remarks: "实例名称")  {constraints(nullable:"false")}  
            column(name: "config_id", type: "BIGINT(20)",  remarks: "配置文件ID")  {constraints(nullable:"false")}  
            column(name: "mount_path", type: "VARCHAR(480)",  remarks: "挂载路径")  {constraints(nullable:"false")}  
            column(name: "config_group", type: "VARCHAR(60)",  remarks: "配置分组")  {constraints(nullable:"false")}  
            column(name: "config_code", type: "VARCHAR(120)",  remarks: "配置编码")  {constraints(nullable:"false")}  
            column(name: "created_by", type: "BIGINT(20)",   defaultValue:"-1",   remarks: "创建人")  {constraints(nullable:"false")}  
            column(name: "last_updated_by", type: "BIGINT(20)",   defaultValue:"-1",   remarks: "最近更新人")  {constraints(nullable:"false")}  
            column(name: "creation_date", type: "DATETIME",   defaultValueComputed :"CURRENT_TIMESTAMP",   remarks: "创建时间")  {constraints(nullable:"false")}  
            column(name: "last_update_date", type: "DATETIME",   defaultValueComputed :"CURRENT_TIMESTAMP",   remarks: "最近更新时间")  {constraints(nullable:"false")}  
            column(name: "object_version_number", type: "BIGINT(20)",   defaultValue:"1",   remarks: "行版本号，用来处理锁")  {constraints(nullable:"false")}  
        }
       createIndex(tableName: "devops_deploy_config", indexName: "devops_deploy_config_n1") {
           column(name: "instance_name")
           column(name: "deploy_object_key")
           column(name: "host_id")
           column(name: "project_id")
       }
    }
    changeSet(author: "jian.zhang02@hand-china.com", id: "devops_deploy_config-2021-10-28-version-2") {
        addColumn (tableName: "devops_deploy_config") {
            column (name: "instance_id", type: "BIGINT(20) UNSIGNED", remarks: "实例ID", afterColumn: "deploy_object_key") {
                constraints (nullable: "false")
            }
        }
    }
    changeSet(author: "jian.zhang02@hand-china.com", id: "devops_deploy_config-2021-11-02-version-3") {
        dropNotNullConstraint (tableName: "devops_deploy_config", columnName: "deploy_object_key", columnDataType: "VARCHAR(120)")
    }
}
