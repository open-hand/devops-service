package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_content.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_content", remarks: 'devops_ci_content') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: 'ci_content_file', type: 'TEXT', remarks: '流水线gitlab-ci.yaml配置文件')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


    changeSet(author: 'wx', id: '2021-06-24-fix-data') {
        sql("""
            UPDATE devops_ci_content dcc 
            SET dcc.ci_content_file = REPLACE ( dcc.ci_content_file, "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.9.1", "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.4" ) 
            WHERE
            dcc.ci_content_file LIKE '%registry.cn-shanghai.aliyuncs.com/c7n/cibase%';
            
            UPDATE devops_ci_content dcc 
            SET dcc.ci_content_file = REPLACE ( dcc.ci_content_file, "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.10.1", "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.4" ) 
            WHERE
            dcc.ci_content_file LIKE '%registry.cn-shanghai.aliyuncs.com/c7n/cibase%';
            
            UPDATE devops_ci_content dcc 
            SET dcc.ci_content_file = REPLACE ( dcc.ci_content_file, "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.0", "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.4" ) 
            WHERE
            dcc.ci_content_file LIKE '%registry.cn-shanghai.aliyuncs.com/c7n/cibase%';

            UPDATE devops_ci_content dcc 
            SET dcc.ci_content_file = REPLACE ( dcc.ci_content_file, "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.1", "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.4" ) 
            WHERE
            dcc.ci_content_file LIKE '%registry.cn-shanghai.aliyuncs.com/c7n/cibase%';
            
            UPDATE devops_ci_content dcc 
            SET dcc.ci_content_file = REPLACE ( dcc.ci_content_file, "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.2", "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.4" ) 
            WHERE
            dcc.ci_content_file LIKE '%registry.cn-shanghai.aliyuncs.com/c7n/cibase%';
            
            UPDATE devops_ci_content dcc 
            SET dcc.ci_content_file = REPLACE ( dcc.ci_content_file, "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.3", "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.11.4" ) 
            WHERE
            dcc.ci_content_file LIKE '%registry.cn-shanghai.aliyuncs.com/c7n/cibase%';

        """)
    }
}