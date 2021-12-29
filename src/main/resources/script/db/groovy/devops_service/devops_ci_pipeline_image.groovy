package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_image.groovy') {
    changeSet(author: 'scp', id: '2020-07-20-create-table') {
        createTable(tableName: "devops_ci_pipeline_image", remarks: 'ci任务生成镜像记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlabPipelineId')
            column(name: "job_name", type: "VARCHAR(255)", remarks: "任务名称")
            column(name: "image_tag", type: "VARCHAR(255)", remarks: "镜像")
            column(name: "harbor_repo_id", type: "BIGINT UNSIGNED", remarks: "镜像仓库id")
            column(name: "repo_type", type: "VARCHAR(20)", remarks: "镜像仓库类型")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_image',
                constraintName: 'uk_gitlab_pipeline_id', columnNames: 'gitlab_pipeline_id,job_name')
    }

    changeSet(author: 'wanghao', id: '2021-12-15-add-column') {
        addColumn(tableName: 'devops_ci_pipeline_image') {
            column(name: "app_service_id", type: "BIGINT UNSIGNED")
        }
    }
    changeSet(author: 'wanghao', id: '2021-12-15-fix-data') {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "devops_ci_pipeline_record")
        }
        sql("""
            update devops_ci_pipeline_image dcpi
            SET dcpi.app_service_id = 
            (SELECT dcp.app_service_id 
                FROM devops_ci_pipeline_record dcpr 
                JOIN devops_cicd_pipeline dcp on dcpr.ci_pipeline_id = dcp.id
                WHERE dcpr.gitlab_pipeline_id = dcpi.gitlab_pipeline_id 
                limit 1)
        """)
    }
    changeSet(author: 'wanghao', id: '2021-12-15-modify-unique-index') {
        dropUniqueConstraint(tableName: 'devops_ci_pipeline_image',
                constraintName: 'uk_gitlab_pipeline_id')
        addUniqueConstraint(tableName: 'devops_ci_pipeline_image',
                constraintName: 'uk_devops_gitlab_pipeline_id', columnNames: 'app_service_id,gitlab_pipeline_id,job_name')
    }
}
