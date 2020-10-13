package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_job_artifact_record.groovy') {
    changeSet(author: 'zmf', id: '2020-04-20-create-table-artifact') {
        createTable(tableName: "devops_ci_job_artifact_record", remarks: 'ci job执行过程中生成的软件包纪录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab流水线记录id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '软件包名称，和流水线中定义的元数据一致') {
                constraints(nullable: false)
            }
            column(name: 'file_url', type: 'VARCHAR(255)', remarks: '上传到文件服务后的文件地址') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_job_artifact_record',
                constraintName: 'uk_artifact_ci_pipeline_id_name', columnNames: 'gitlab_pipeline_id,name')
    }

    changeSet(author: "zmf", id: "2020-04-26-add-gitlab-job-id") {
        addColumn(tableName: 'devops_ci_job_artifact_record') {
            column(name: 'gitlab_job_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab job id', afterColumn: 'gitlab_pipeline_id') {
                constraints(nullable: false)
            }
        }
        createIndex(indexName: "artifact_record_idx_job_id_name ", tableName: "devops_ci_job_artifact_record") {
            column(name: "gitlab_job_id")
        }
    }

    changeSet(author: "zmf", id: "2020-09-13-delete-ci-artifact-table") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "devops_ci_job_artifact_record")
        }
        dropTable(tableName: "devops_ci_job_artifact_record")
    }
}