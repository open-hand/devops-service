package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_step.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table') {
        createTable(tableName: "devops_ci_step", remarks: 'CI步骤表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')

            column(name: 'name', type: 'VARCHAR(255)', remarks: '步骤名称') {
                constraints(nullable: false)
            }
            column(name: 'devops_ci_job_id', type: 'BIGINT UNSIGNED', remarks: 'devops流水线任务id') {
                constraints(nullable: false)
            }

            column(name: 'type', type: 'VARCHAR(255)', remarks: '任务类型 build 构建，sonar 代码检查, chart chart发布, custom 自定义') {
                constraints(nullable: false)
            }

            column(name: 'script', type: 'TEXT', remarks: '步骤中包含的脚本')

            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '步骤的顺序') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_devops_ci_job_id ", tableName: "devops_ci_step") {
            column(name: "devops_ci_job_id")
        }
    }
    changeSet(author: 'wanghao', id: '2023-04-04-update-column') {
        sql("""
            UPDATE devops_ci_step INNER JOIN devops_ci_sonar_config ON devops_ci_step.id = devops_ci_sonar_config.step_id 
SET devops_ci_step.script = "sonar-scanner -Dsonar.host.url=\${SONAR_URL} -Dsonar.login=\${SONAR_LOGIN} -Dsonar.password=\${SONAR_PASSWORD} -Dsonar.projectKey=\${SONAR_PROJECT_KEY} -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=\${SONAR_SOURCES} -Dsonar.qualitygate.wait=\${SONAR_QUALITYGATE_WAIT_FLAG}"
WHERE devops_ci_sonar_config.scanner_type = 'SonarScanner'
        """)
        sql("""
            UPDATE devops_ci_step INNER JOIN devops_ci_sonar_config ON devops_ci_step.id = devops_ci_sonar_config.step_id 
SET devops_ci_step.script = "# 如果了配置maven仓库,运行时会下载settings.xml到根目录，此时可以使用-s settings.xml指定使用
#一、Java 8项目扫描指令
#使用java8进行编译，如果是多模块项目则使用install命令
export JAVA_HOME=/opt/java/openjdk8
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify -Dmaven.test.failure.ignore=true -DskipTests=\${SONAR_SKIP_TEST_FLAG}
#使用java11进行扫描
export JAVA_HOME=/opt/java/openjdk
mvn sonar:sonar -Dsonar.host.url=\${SONAR_URL} -Dsonar.login=\${SONAR_LOGIN} -Dsonar.password=\${SONAR_PASSWORD} -Dsonar.projectKey=\${SONAR_PROJECT_KEY} -Dsonar.qualitygate.wait=\${SONAR_QUALITYGATE_WAIT_FLAG}

#Java 11项目扫描指令
#mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify sonar:sonar -Dsonar.host.url=\${SONAR_URL} -Dsonar.login=\${SONAR_LOGIN} -Dsonar.password=\${SONAR_PASSWORD} -Dsonar.projectKey=\${SONAR_PROJECT_KEY} -Dsonar.qualitygate.wait=\${SONAR_QUALITYGATE_WAIT_FLAG} -Dmaven.test.failure.ignore=true -DskipTests=\${SONAR_SKIP_TEST_FLAG}"
WHERE devops_ci_sonar_config.scanner_type = 'SonarMaven'
        """)
    }

}