package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_env.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_env", remarks: '环境管理') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，环境ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目 ID')
            column(name: 'name', type: 'VARCHAR(32)', remarks: '环境名称')
            column(name: 'code', type: 'VARCHAR(32)', remarks: '环境编码')
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '序号')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: '命名空间')
            column(name: 'token', type: 'CHAR(36)', remarks: 'token')
            column(name: 'description', type: 'VARCHAR(64)', remarks: '环境描述')
            column(name: 'is_active', type: 'TINYINT UNSIGNED', remarks: '是否可用')
            column(name: 'is_connected', type: 'TINYINT UNSIGNED', remarks: '环境状态')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_name', columnNames: 'project_id,name')
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_code', columnNames: 'project_id,code')
        createIndex(indexName: "idx_project_id", tableName: "devops_env") {
            column(name: "project_id")
        }
    }

    changeSet(id: '2018-05-20-drop-constraint', author: 'younger') {
        dropUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_code')

    }

    changeSet(author: 'younger', id: '2018-05-21-drop-column')
            {
                dropColumn(columnName: "code", tableName: "devops_env")
            }
    changeSet(id: '2018-05-21-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'varchar(128)', newColumnName: 'code', oldColumnName: 'namespace', remarks: '环境命名空间', tableName: 'devops_env')
    }
    changeSet(id: '2018-05-21-add-constraint', author: 'younger') {
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'uk_project_id_code', columnNames: 'project_id,code')

    }

    changeSet(author: 'younger', id: '2018-07-25-add-column')
            {
                addColumn(tableName: 'devops_env') {
                    column(name: 'gitlab_env_project_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab env project id', afterColumn: 'project_id')
                    column(name: 'env_id_rsa', type: 'varchar(5000)', remarks: 'ssh id rsa ', afterColumn: 'token')
                    column(name: 'env_id_rsa_pub', type: 'varchar(5000)', remarks: 'ssh id rsa pub', afterColumn: 'env_id_rsa')
                    column(name: 'hook_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab webhook', afterColumn: 'gitlab_env_project_id')
                }
            }
    changeSet(author: 'younger', id: '2018-08-1-add-column')
            {
                addColumn(tableName: 'devops_env') {
                    column(name: 'git_commit', type: 'BIGINT UNSIGNED', remarks: 'env_commit_id', afterColumn: 'hook_id')
                    column(name: 'devops_sync_commit', type: 'BIGINT UNSIGNED', remarks: 'env_commit_devops_sync_id', afterColumn: 'git_commit')
                    column(name: 'agent_sync_commit', type: 'BIGINT UNSIGNED', remarks: 'senv_commit_agent_sync_id', afterColumn: 'devops_sync_commit')
                }
            }


    changeSet(author: 'younger', id: '2018-09-03-modify-index') {
        dropIndex(indexName: "idx_project_id",tableName: "devops_env")

        createIndex(indexName: "devops_env_idx_project_id", tableName: "devops_env") {
            column(name: "project_id")
        }
    }

    changeSet(author: 'younger', id: '2018-09-04-add-column')
            {
                addColumn(tableName: 'devops_env') {
                    column(name: 'devops_env_group_id', type: 'BIGINT UNSIGNED', remarks: 'devops env group id', afterColumn: 'agent_sync_commit')
                }
            }

    changeSet(id: '2018-10-08-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'saga_sync_commit', oldColumnName: 'git_commit', remarks: 'saga同步的commit', tableName: 'devops_env')
    }

    changeSet(author: 'younger', id: '2018-11-01-add-column')
            {
                addColumn(tableName: 'devops_env') {
                    column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '集群id', afterColumn: 'project_id')
                }
                dropUniqueConstraint(constraintName: "uk_project_id_code",tableName: "devops_env")
                addUniqueConstraint(tableName: 'devops_env',
                        constraintName: 'devops_envs_uk_cluster_id_code', columnNames: 'cluster_id,code')
            }

    changeSet(author: 'n1ck',id: '2018-11-20-modicy-column') {
        sql("ALTER TABLE devops_env MODIFY COLUMN `name` VARCHAR(32) BINARY")
    }

    changeSet(author: 'younger', id: '2018-11-21-add-column') {
        addColumn(tableName: 'devops_env') {
            column(name: 'is_synchro', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: 'is synchro', afterColumn: 'gitlab_env_project_id')
            column(name: 'is_failed', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: 'is failed', afterColumn: 'is_synchro')
        }
        dropColumn(columnName: "is_connected", tableName: "devops_env")
        sql("UPDATE devops_env  de SET de.is_synchro= (CASE when de.gitlab_env_project_id  is not null THEN 1  else  0  END)")
        sql("UPDATE devops_env  de SET de.is_failed= (CASE when de.gitlab_env_project_id  is  null THEN 1  else  0  END)")
    }

    changeSet(author: 'zmf', id: '2018-12-13-alter-unique-constraint') {
        dropUniqueConstraint(constraintName: "uk_project_id_name",tableName: "devops_env")
    }


    changeSet(author: 'younger', id: '2019-04-08-drop-constraint')
            {
                preConditions (onFail: 'MARK_RAN') {
                    indexExists(tableName: "devops_env",indexName: "devops_envs_uk_cluster_id_code")
                }
                dropUniqueConstraint(constraintName: "devops_envs_uk_cluster_id_code", tableName: "devops_env")
                addUniqueConstraint(tableName: 'devops_env',
                        constraintName: 'devops_envs_uk_cluster_and_project_code', columnNames: 'cluster_id,project_id,code')
            }

    changeSet(author: 'younger', id: '2019-07-30-drop-column') {
        dropColumn(columnName: "sequence", tableName: "devops_env")
    }

    changeSet(author: 'zmf', id: '2019-07-29-add-is-skip-check-permission') {
        addColumn(tableName: 'devops_env') {
            column(name: 'is_skip_check_permission', type: 'TINYINT UNSIGNED', defaultValue: '0', remarks: '是否跳过环境权限校验', afterColumn: 'is_failed')
        }
    }

    changeSet(author: 'zmf', id: '2019-09-18-add-default-value-for-env-active') {
        addDefaultValue(tableName: "devops_env", columnName: "is_active", defaultValue: "1")
    }

    changeSet(author: 'sheep', id: '2019-09-29-updateDataType') {
        modifyDataType(tableName: 'devops_env', columnName: 'description', newDataType: 'VARCHAR(500)')
    }

    changeSet(author: 'scp', id: '2019-10-23-addColumn') {
        addColumn(tableName: 'devops_env') {
            column(name: 'type', type: 'varchar(10)', defaultValue: 'user', remarks: '环境类型', afterColumn: 'is_failed')
        }
    }

    changeSet(author: 'zmf', id: '2019-12-12-add-project-code-type-constraint') {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: "devops_env", columnName:"project_id")
            columnExists(tableName: "devops_env", columnName:"code")
            columnExists(tableName: "devops_env", columnName:"type")
            sqlCheck(expectedResult: "0", sql: """
                SELECT COUNT(1)
                FROM (SELECT 1
                FROM devops_env duplication
                WHERE duplication.project_id IS NOT NULL
                AND duplication.code IS NOT NULL
                AND duplication.type IS NOT NULL
                GROUP BY duplication.project_id, duplication.code, duplication.type
                HAVING COUNT(1) > 1) tmp""")
        }
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'devops_env_uk_project_code_type', columnNames: 'project_id,code,type')
    }

    changeSet(author: 'zmf', id: '2019-12-12-add-cluster-code-constraint') {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: "devops_env", columnName:"cluster_id")
            columnExists(tableName: "devops_env", columnName:"code")
            sqlCheck(expectedResult: "0", sql: """
                SELECT COUNT(1)
                FROM (SELECT 1
                FROM devops_env duplication
                WHERE duplication.cluster_id IS NOT NULL
                AND duplication.code IS NOT NULL
                GROUP BY duplication.cluster_id, duplication.code
                HAVING COUNT(1) > 1) tmp""")
        }
        addUniqueConstraint(tableName: 'devops_env',
                constraintName: 'devops_env_uk_cluster_code', columnNames: 'cluster_id,code')
    }

    changeSet(author: 'zmf', id: '2019-12-12-drop-devops-env-constraint') {
        preConditions(onFail: 'MARK_RAN') {
            indexExists(tableName: "devops_env", indexName: "devops_envs_uk_cluster_and_project_code")
            indexExists(tableName: "devops_env", indexName: "devops_env_uk_project_code_type")
            indexExists(tableName: "devops_env", indexName: "devops_env_uk_cluster_code")
        }
        dropUniqueConstraint(constraintName: "devops_envs_uk_cluster_and_project_code", tableName: "devops_env")
    }

    changeSet(author: 'lihao', id: '2020-07-24-devops_env-modify-column') {
        sql("ALTER TABLE devops_env MODIFY COLUMN `name` VARCHAR(128)")
    }

    changeSet(author: 'scp', id: '2021-07-13-addColumn') {
        addColumn(tableName: 'devops_env') {
            column(name: 'is_auto_deploy', type: 'TINYINT UNSIGNED', defaultValue: '1', remarks: '一键开启或者关闭自动部署，1：开启，0：关闭', afterColumn: 'is_active')
        }
    }

    changeSet(author: 'lihao', id: '2021-11-02-drop-index') {
        dropIndex(indexName: "devops_env_idx_project_id", tableName: "devops_env")
    }

    changeSet(author: 'scp', id: '2021-12-10-fix-auto-deploy') {
        sql("UPDATE devops_env SET is_auto_deploy = 1 WHERE is_auto_deploy IS NULL;")
    }

    changeSet(author: 'lihao', id: '2023-01-30-addColumn') {
        addColumn(tableName: 'devops_env') {
            column(name: 'check_values_policy', type: 'TINYINT UNSIGNED', defaultValue: '0', remarks: '是否开启确认values生效策略，1：开启，0：关闭', afterColumn: 'is_active')
        }
    }
}