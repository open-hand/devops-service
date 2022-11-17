package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_job_record.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_job_record", remarks: 'CI任务记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_job_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab job id')
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab_流水线记录id')
            column(name: 'name', type: 'VARCHAR(255)', remarks: '任务名称')
            column(name: 'stage', type: 'VARCHAR(255)', remarks: '所属阶段名称')
            column(name: 'status', type: 'VARCHAR(255)', remarks: 'job状态')
            column(name: 'trigger_user_id', type: 'BIGINT UNSIGNED', remarks: '触发用户id')
            column(name: "started_date", type: "DATETIME", remarks: 'job开始执行时间')
            column(name: "finished_date", type: "DATETIME", remarks: 'job结束时间')
            column(name: "duration_seconds", type: "BIGINT UNSIGNED", remarks: 'job执行时长')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_job_record',
                constraintName: 'uk_gitlab_job_id', columnNames: 'gitlab_job_id')
    }
    changeSet(author: 'wanghao', id: '2020-04-07-rename-column') {
        renameColumn(tableName: 'devops_ci_job_record', columnDataType: 'BIGINT UNSIGNED',
                oldColumnName: 'gitlab_pipeline_id', newColumnName: 'ci_pipeline_record_id')
    }
    changeSet(author: 'wanghao', id: '2020-04-09-add-column') {
        addColumn(tableName: 'devops_ci_job_record') {
            column(name: "type", type: "VARCHAR(255)", remarks: '任务类型')
        }
    }
    changeSet(author: 'wanghao', id: '2020-04-13-add-column') {
        addColumn(tableName: 'devops_ci_job_record') {
            column(name: "gitlab_project_id", type: "BIGINT UNSIGNED", remarks: 'gitlab_project_id')
        }
        createIndex(tableName: 'devops_ci_job_record', indexName: 'ci_job_record_gpid_idx') {
            column(name: 'gitlab_project_id')
        }
    }
    changeSet(author: 'wanghao', id: '2020-04-19-add-not-null-cons') {
        addNotNullConstraint(tableName: "devops_ci_job_record", columnName: "name", columnDataType: "VARCHAR(255)")
    }
    changeSet(author: 'wanghao', id: '2020-08-26-add-column') {
        addColumn(tableName: 'devops_ci_job_record') {
            column(name: "metadata", type: "TEXT", afterColumn: 'duration_seconds')
        }
    }
    changeSet(author: 'wanghao', id: '2020-11-10-add-index') {
        createIndex(indexName: "idx_ci_pipeline_record_id ", tableName: "devops_ci_job_record") {
            column(name: "ci_pipeline_record_id")
        }
    }

    changeSet(author: 'wx', id: '2021-04-14-add-column') {
        addColumn(tableName: 'devops_ci_job_record') {
            column(name: "maven_setting_id", type: 'BIGINT UNSIGNED', afterColumn: 'duration_seconds', defaultValue: "0")
        }
    }

    changeSet(author: 'wanghao', id: '2021-11-1-add-column') {
        addColumn(tableName: 'devops_ci_job_record') {
            column(name: "app_service_id", type: 'BIGINT UNSIGNED', afterColumn: 'duration_seconds')
        }
    }

    changeSet(author: 'wanghao', id: '2021-11-1-fix-column') {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "devops_ci_pipeline_record")
            tableExists(tableName: "devops_cicd_pipeline")
        }
        sql("""
            UPDATE 
            devops_ci_job_record dcjr 
            set dcjr.app_service_id = 
            (SELECT dcp.app_service_id
            FROM devops_ci_pipeline_record dcpr
            JOIN devops_cicd_pipeline dcp ON dcp.id = dcpr.ci_pipeline_id
            WHERE dcpr.id = dcjr.ci_pipeline_record_id
            GROUP BY dcp.app_service_id)
        """)
    }

    changeSet(author: 'wanghao', id: '2021-11-1-add-constraint') {
        addNotNullConstraint(tableName: "devops_ci_job_record", columnName: "app_service_id", columnDataType: "BIGINT UNSIGNED")
    }


    changeSet(author: 'wanghao', id: '2021-11-2-modify-unique-index') {
        dropUniqueConstraint(tableName: 'devops_ci_job_record',
                constraintName: 'uk_gitlab_job_id')
        addUniqueConstraint(tableName: 'devops_ci_job_record',
                constraintName: 'uk_gitlab_job_app_service_id', columnNames: 'gitlab_job_id, app_service_id')
    }

    changeSet(author: 'wanghao', id: '2021-12-29-add-column') {

        addColumn(tableName: 'devops_ci_job_record') {
            column(name: 'group_type', type: 'VARCHAR(20)', remarks: '分组类型', afterColumn: 'stage')
        }
    }
    changeSet(author: 'wanghao', id: '2022-1-9-fix-data') {
        sql("""
            UPDATE devops_ci_job_record dcjr set dcjr.type = 'normal' WHERE dcjr.type != 'custom'
        """)
    }
    changeSet(author: 'wanghao', id: '2022-11-09-add-column') {

        addColumn(tableName: 'devops_ci_job_record') {
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '部署操作commandId', afterColumn: 'trigger_user_id')
        }
    }

    changeSet(author: 'lihao', id: '2022-11-14-add-column') {
        addColumn(tableName: 'devops_ci_job_record') {
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '配置id', afterColumn: 'command_id')
        }
    }

    changeSet(author: 'lihao', id: '2022-11-16-add-column') {
        addColumn(tableName: 'devops_ci_job_record') {
            column(name: 'api_test_task_record_id', type: 'BIGINT UNSIGNED', remarks: 'api测试任务记录id', afterColumn: 'config_id')
        }
    }
    changeSet(author: 'wanghao', id: '2022-11-17-drop-column') {
        dropColumn(columnName: "metadata", tableName: "devops_ci_job_record")
    }
}