package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_job.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_job", remarks: 'CI任务表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '任务名称')
            column(name: 'ci_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: 'ci_stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段id')
            column(name: 'type', type: 'VARCHAR(255)', remarks: '任务类型 build 构建，sonar 代码检查')
            column(name: 'trigger_refs', type: 'VARCHAR(255)', remarks: '触发分支')
            column(name: 'metadata', type: 'VARCHAR(2000)', remarks: 'job详细信息，定义了job执行内容')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_job',
                constraintName: 'uk_pipeline_id_job_name', columnNames: 'ci_pipeline_id,name')
    }
    changeSet(author: 'wanghao', id: '2020-04-08-change-column') {
        modifyDataType(tableName: 'devops_ci_job', columnName: 'metadata', newDataType: 'TEXT')
    }

    changeSet(author: 'zmf', id: '2020-04-28-job-add-image') {
        addColumn(tableName: 'devops_ci_job') {
            column(name: 'image', type: 'VARCHAR(280)', remarks: 'job的镜像地址') {
                constraints(nullable: true)
            }
        }
    }


    changeSet(author: 'zmf', id: '2020-06-15-job-add-upload-and-download') {
        addColumn(tableName: 'devops_ci_job') {
            column(name: 'is_to_upload', type: 'TINYINT(1) UNSIGNED', defaultValue: "0", remarks: '是否上传共享目录的内容, 默认为false', afterColumn: 'trigger_refs') {
                constraints(nullable: false)
            }
            column(name: 'is_to_download', type: 'TINYINT(1) UNSIGNED', defaultValue: "0", remarks: '是否下载共享目录的内容,默认为false', afterColumn: 'is_to_upload') {
                constraints(nullable: false)
            }
        }
    }

    changeSet(author: 'zmf', id: '2020-06-18-job-add-trigger') {
        addColumn(tableName: 'devops_ci_job') {
            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发方式', afterColumn: 'trigger_refs', defaultValue: 'refs')
        }
        renameColumn(columnDataType: 'VARCHAR(255)', newColumnName: 'trigger_value', oldColumnName: 'trigger_refs', remarks: '触发方式对应的值', tableName: 'devops_ci_job')
        sql("UPDATE devops_ci_job dcj SET dcj.trigger_type = 'refs' WHERE dcj.trigger_value IS NOT NULL")
        sql("UPDATE devops_ci_job dcj SET dcj.trigger_type = 'refs' WHERE dcj.trigger_type IS NULL")
    }

    changeSet(author: 'wanghao', id: '2021-11-18-add-column') {
        addColumn(tableName: 'devops_ci_job') {
            column(name: 'parallel', type: 'BIGINT UNSIGNED', remarks: '并发数', afterColumn: 'trigger_type')
        }
    }

    changeSet(author: 'wanghao', id: '2021-12-22-add-column') {
        renameColumn(columnDataType: 'VARCHAR(255)', newColumnName: 'old_type', oldColumnName: 'type', remarks: '任务类型', tableName: 'devops_ci_job')

        addColumn(tableName: 'devops_ci_job') {
            column(name: 'group_type', type: 'VARCHAR(20)', remarks: '分组类型', afterColumn: 'trigger_type')
            column(name: 'script', type: 'text', remarks: '步骤中包含的脚本', afterColumn: 'group_type')
            column(name: 'type', type: 'VARCHAR(255)', remarks: '任务类型 normal 普通，custom 自定义脚本')
        }
    }
    changeSet(author: 'wanghao', id: '2022-11-04-add-column') {
        addColumn(tableName: 'devops_ci_job') {
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '任务关联的配置id')
        }
    }

    changeSet(author: 'lihao', id: '20220-11-08-add-column') {
        addColumn(tableName: 'devops_ci_job') {
            column(name: 'start_in', type: 'int(5)', remarks: '任务启动延时时间')
        }
    }
}