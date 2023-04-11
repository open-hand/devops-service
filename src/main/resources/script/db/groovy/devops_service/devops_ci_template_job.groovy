package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_job.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_job') {
        createTable(tableName: "devops_ci_template_job", remarks: '流水线任务模板表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(60)', remarks: '任务名称') {
                constraints(nullable: false)
            }
            column(name: 'group_id', type: 'BIGINT UNSIGNED', remarks: '任务分组id') {
                constraints(nullable: false)
            }
            column(name: 'image', type: 'VARCHAR(500)', remarks: '流水线模板镜像地址')

            column(name: 'source_type', type: 'VARCHAR(12)', remarks: '层级') {
                constraints(nullable: false)
            }
            column(name: 'source_id', type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }

            column(name: 'built_in', type: 'TINYINT UNSIGNED', remarks: '是否预置，1:预置，0:自定义') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(20)', remarks: '步骤类型') {
                constraints(nullable: false)
            }
            column(name: 'script', type: 'text', remarks: '自定义步骤的脚本')

            column(name: 'to_upload', defaultValue: "0", type: 'TINYINT UNSIGNED', remarks: '是否上传到共享目录') {
                constraints(nullable: false)
            }

            column(name: 'to_download', defaultValue: "0", type: 'TINYINT UNSIGNED', remarks: '是否下载到共享目录') {
                constraints(nullable: false)
            }
            column(name: 'parallel', type: 'BIGINT UNSIGNED', remarks: '并发数')


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_template_job', constraintName: 'uk_name_source_type_source_id', columnNames: 'name,source_type,source_id')
        createIndex(tableName: 'devops_ci_template_job', indexName: 'idx_source_type_source_id') {
            column(name: 'source_type')
            column(name: 'source_id')
        }
        createIndex(tableName: 'devops_ci_template_job', indexName: 'idx_type') {
            column(name: 'type')
        }
        createIndex(tableName: 'devops_ci_template_job', indexName: 'idx_group_id') {
            column(name: 'group_id')
        }
    }
    changeSet(author: 'wanghao', id: '2022-11-03-add-column') {
        addColumn(tableName: 'devops_ci_template_job') {
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '任务关联的配置id', afterColumn: 'parallel')
        }
    }
    changeSet(author: 'wanghao', id: '2022-11-16-add-column') {
        addColumn(tableName: 'devops_ci_template_job') {
            column(name: 'start_in', type: 'int(5)', remarks: '任务启动延时时间', afterColumn: 'config_id')
            column(name: 'tags', type: 'VARCHAR(255)', remarks: 'job的tag标签', afterColumn: 'config_id')
        }
    }

    changeSet(author: 'wx', id: '2022-11-18-add-column-visibility') {
        addColumn(tableName: 'devops_ci_template_job') {
            column(name: 'visibility', type: 'TINYINT UNSIGNED', defaultValue: "1", remarks: '可见性，1:可见，0:不可见', afterColumn: 'built_in') {
                constraints(nullable: false)
            }
            column(name: 'trigger_value', type: 'VARCHAR(255)', remarks: '触发分支', afterColumn: 'type')
            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发方式', afterColumn: 'trigger_value', defaultValue: 'refs')
        }
    }
    changeSet(author: 'wx', id: '2022-11-23-update-column') {
        sql("""
              ALTER TABLE devops_ci_template_job ALTER COLUMN type 
               SET DEFAULT 'normal'
        """)
    }
    changeSet(author: 'wanghao', id: '2023-04-03-add-column') {
        addColumn(tableName: 'devops_ci_template_job') {
            column(name: "is_enabled", type: "TINYINT UNSIGNED", defaultValue: "1", afterColumn: 'parallel', remarks: '是否启用')
        }
    }
}