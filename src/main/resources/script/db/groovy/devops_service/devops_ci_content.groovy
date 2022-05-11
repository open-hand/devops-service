package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_content.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_content", remarks: 'CI流水线内容表') {
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


    changeSet(author: 'wx', id: '2021-06-24-fix-data-content') {
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

    changeSet(author: 'wanghao', id: '2021-11-30-add-column') {
        addColumn(tableName: 'devops_ci_content') {
            column(name: 'pipeline_version_number', type: 'BIGINT UNSIGNED', defaultValue: "1", remarks: '流水线版本号', afterColumn: 'ci_pipeline_id')
            column(name: 'devops_default_rule_number', type: 'BIGINT UNSIGNED', defaultValue: "1", remarks: 'devops默认渲染规则版本号', afterColumn: 'pipeline_version_number')
        }
    }

    changeSet(author: 'wanghao', id: '2021-11-30-add-index') {
        createIndex(indexName: "idx_pipeline_id", tableName: "devops_ci_content") {
            column(name: "ci_pipeline_id")
        }
    }

}