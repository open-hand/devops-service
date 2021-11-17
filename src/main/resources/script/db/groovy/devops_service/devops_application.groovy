package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_application.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_application", remarks: '应用管理') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目 ID')
            column(name: 'name', type: 'VARCHAR(64)', remarks: '应用名称')
            column(name: 'code', type: 'VARCHAR(64)', remarks: '应用编码')
            column(name: 'is_active', type: 'TINYINT UNSIGNED', remarks: '同步状态')
            column(name: 'is_synchro', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: '是否同步成功。1成功，0失败')
            column(name: 'gitlab_project_id', type: 'BIGINT UNSIGNED', remarks: 'GitLab 项目 ID')
            column(name: 'app_template_id', type: 'BIGINT UNSIGNED', remarks: '应用模板 ID')
            column(name: 'uuid', type: 'VARCHAR(50)')
            column(name: 'token', type: 'CHAR(36)', remarks: 'TOKEN')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_project_id ", tableName: "devops_application") {
            column(name: "project_id")
        }
        addUniqueConstraint(tableName: 'devops_application',
                constraintName: 'uk_project_id_name', columnNames: 'project_id,name')
    }

    changeSet(author: 'younger', id: '2018-07-11-add-column') {
        addColumn(tableName: 'devops_application') {
            column(name: 'hook_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab webhook', afterColumn: 'gitlab_project_id')
        }

    }

    changeSet(author: 'younger', id: '2018-09-03-modify-UniqueConstraint') {
        dropUniqueConstraint(constraintName: "uk_project_id_name", tableName: "devops_application")
        addUniqueConstraint(tableName: 'devops_application',
                constraintName: 'devops_app_uk_project_id_name', columnNames: 'project_id,name')
    }

    changeSet(author: 'younger', id: '2018-09-03-modify-index') {
        dropIndex(indexName: "idx_project_id", tableName: "devops_application")

        createIndex(indexName: "devops_app_idx_project_id", tableName: "devops_application") {
            column(name: "project_id")
        }
    }

    changeSet(author: 'crockitwood', id: '2018-09-29-add-column') {
        addColumn(tableName: 'devops_application') {
            column(name: 'is_failed', type: 'TINYINT UNSIGNED', remarks: '是否创建失败', afterColumn: 'is_synchro')
        }

    }

    changeSet(author: 'n1ck', id: '2018-11-20-modify-column-collate') {
        sql("ALTER TABLE devops_application MODIFY COLUMN `name` VARCHAR(64) BINARY")
    }

    changeSet(author: 'younger', id: '2018-11-22-add-column') {
        addColumn(tableName: 'devops_application') {
            column(name: 'type', type: 'VARCHAR(50)', remarks: '应用类型', afterColumn: 'code')
        }
        sql("UPDATE devops_application  da SET da.type = 'normal'")
    }

    changeSet(author: 'n1ck', id: '2018-11-23-add-column') {
        addColumn(tableName: 'devops_application') {
            column(name: 'is_skip_check_permission', type: 'TINYINT UNSIGNED', remarks: '是否跳过权限检查', afterColumn: 'is_failed')
        }
    }

    changeSet(author: 'n1ck', id: '2018-12-12-set-default-for-is_skip_check_permission') {
        // remarks: '为之前的is_skip_check_permission字段设置默认值'
        sql("UPDATE devops_application da SET da.is_skip_check_permission = FALSE WHERE da.is_skip_check_permission IS NULL")
    }


    changeSet(author: '10980', id: '2019-3-13-add-column') {
        addColumn(tableName: 'devops_application') {
            column(name: 'harbor_config_id', type: 'BIGINT UNSIGNED', remarks: 'harbor配置信息', afterColumn: 'app_template_id')
            column(name: 'chart_config_id', type: 'BIGINT UNSIGNED', remarks: 'chart配置信息', afterColumn: 'harbor_config_id')
        }
    }

    changeSet(author: 'scp', id: '2019-7-29-rename-table') {
        addColumn(tableName: 'devops_application') {
            column(name: 'img_url', type:  'VARCHAR(200)', remarks: '图标url', afterColumn: 'is_failed')
        }
        renameTable(newTableName: 'devops_app_service', oldTableName: 'devops_application')

    }

    changeSet(author: 'Younger', id: '2019-8-05-drop-column') {
        dropColumn(columnName: "app_template_id", tableName: "devops_app_service")
    }

    changeSet(author: 'scp', id: '2019-09-17-add-column') {
        addColumn(tableName: 'devops_app_service') {
            column(name: 'mkt_app_id', type:  'BIGINT UNSIGNED', remarks: '应用市场应用Id', afterColumn: 'is_failed')
        }
    }

    changeSet(author: 'zmf', id: '2019-09-18-add-default-value-for-failed') {
        addDefaultValue(tableName: "devops_app_service", columnName: "is_failed", defaultValue: "0")
    }

    changeSet(author: 'zmf', id: '2019-09-18-add-default-value-for-app-service-active') {
        addDefaultValue(tableName: "devops_app_service", columnName: "is_active", defaultValue: "1")
    }

    changeSet(author: 'zmf', id: '2020-05-13-app-add-uk', failOnError: false) {
        addUniqueConstraint(tableName: 'devops_app_service', constraintName: 'app-service-token-uk', columnNames: 'token')
    }
    changeSet(author: 'wanghao', id: '2020-07-05-app-service-add--index') {
        createIndex(indexName: "idx_last_update_by", tableName: "devops_app_service") {
            column(name: "last_updated_by")
            column(name: 'last_update_date')
        }
    }
    changeSet(author: 'wanghao', id: '2020-07-06-app-service-add-index') {
        createIndex(indexName: "idx_gitlab_project_id", tableName: "devops_app_service") {
            column(name: 'gitlab_project_id')
        }
    }

    changeSet(author: 'zmf', id: '2021-04-06-add-gitlab-project-unique-index') {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "devops_app_service")
            indexExists(tableName: "devops_app_service", indexName: "idx_gitlab_project_id")
            sqlCheck(expectedResult: "0", sql: """
                SELECT COUNT(1)
                FROM (SELECT COUNT(1)
                FROM devops_app_service das
                GROUP BY gitlab_project_id
                HAVING COUNT(gitlab_project_id) > 1) tmp""")
        }
        dropIndex(tableName: "devops_app_service", indexName: "idx_gitlab_project_id")
        addUniqueConstraint(tableName: "devops_app_service", constraintName: 'uk_app_gitlab_project_id', columnNames: "gitlab_project_id")
    }

    changeSet(author: 'wx', id: '2021-05-14-add-column') {
        addColumn(tableName: 'devops_app_service') {
            column(name: 'error_message', type:  'text', remarks: '失败的错误信息')
        }
    }

    changeSet(author: 'zmf', id: '2021-05-24-add-columns') {
        addColumn(tableName: 'devops_app_service') {
            column(name: 'group_id', type:  'varchar(512)', remarks: '应用服务附加的pom信息：groupId（敏捷使用）', afterColumn: 'token')
            column(name: 'artifact_id', type:  'varchar(512)', remarks: '应用服务附加的pom信息：artifactId（敏捷使用）', afterColumn: 'group_id')
        }
    }

    changeSet(author: 'wanghao', id: '2021-09-29-drop-unique-constraint') {
        preConditions(onFail: "MARK_RAN") {
            indexExists(tableName: "devops_app_service", indexName: "uk_app_gitlab_project_id")
        }
        dropIndex(tableName: "devops_app_service", indexName: "uk_app_gitlab_project_id")
    }

    changeSet(author: 'wanghao', id: '2021-09-29-add-columns') {
        addColumn(tableName: 'devops_app_service') {
            column(name: 'external_gitlab_url', type:  'varchar(512)', defaultValue: "none" ,remarks: '外部平台gitlab地址，不为空则是外部应用', afterColumn: 'token')
            column(name: 'external_config_id', type:  'BIGINT UNSIGNED' ,remarks: '外部配置id', afterColumn: 'external_gitlab_url')
        }
        sql("""
            UPDATE 
            devops_app_service das
            SET das.external_gitlab_url = UUID()
            WHERE das.id IN 
            (SELECT * FROM
            (SELECT d.id
            from devops_app_service d
            WHERE d.gitlab_project_id IN 
            (select  ds1.gitlab_project_id 
            from devops_app_service ds1 group by ds1.gitlab_project_id having count(ds1.gitlab_project_id) > 1)) tmp)
        """)

        addUniqueConstraint(tableName: "devops_app_service", constraintName: 'uk_app_gitlab_project_id_and_url', columnNames: "gitlab_project_id, external_gitlab_url")
        addUniqueConstraint(tableName: "devops_app_service", constraintName: 'uk_external_config_id', columnNames: "external_config_id")

    }

    changeSet(author: 'lihao',id: '2021-11-02-drop-index'){
            dropIndex(indexName: "devops_app_idx_project_id", tableName: "devops_app_service")
    }
}