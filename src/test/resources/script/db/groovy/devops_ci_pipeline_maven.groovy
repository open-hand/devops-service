package script.db.groovy

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_maven.groovy') {
    changeSet(author: 'scp', id: '2020-07-21-create-table') {
        createTable(tableName: "devops_ci_pipeline_maven", remarks: 'ci任务推送maven记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlabPipelineId')
            column(name: "job_name", type: "VARCHAR(255)", remarks: "任务名称")
            column(name: "group_id", type: "VARCHAR(60)", remarks: "groupId")
            column(name: "artifact_id", type: "VARCHAR(60)", remarks: "artifactId")
            column(name: "version", type: "VARCHAR(60)", remarks: "版本")
            column(name: "nexus_repo_id", type: "BIGINT UNSIGNED", remarks: "nexus仓库id")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_maven',
                constraintName: 'pipeline_maven_uk_gitlab_pipeline_id', columnNames: 'gitlab_pipeline_id,job_name')
    }
}
