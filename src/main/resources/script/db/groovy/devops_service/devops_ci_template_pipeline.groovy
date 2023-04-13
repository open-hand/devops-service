package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_pipeline.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_pipeline') {
        createTable(tableName: "devops_ci_template_pipeline", remarks: '流水线模板表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(120)', remarks: '流水线模板名称') {
                constraints(nullable: false)
            }
            column(name: 'source_type', type: 'VARCHAR(12)', remarks: '层级') {
                constraints(nullable: false)
            }
            column(name: 'source_id',  type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }
            column(name: 'built_in',  type: 'TINYINT UNSIGNED', remarks: '是否预置，1:预置，0:自定义') {
                constraints(nullable: false)
            }

            column(name: 'ci_template_category_id', type: 'BIGINT UNSIGNED', remarks: '流水下分类id') {
                constraints(nullable: false)
            }

            column(name: 'enable', type: 'TINYINT UNSIGNED', remarks: '是否启用') {
                constraints(nullable: false)
            }
            column(name: 'version_name', type: 'VARCHAR(255)', remarks: '版本命名规则')

            column(name: 'image', type: 'VARCHAR(500)', remarks: '流水线模板镜像地址')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_template_pipeline', constraintName: 'uk_name_source_type_source_id', columnNames: 'name,source_type,source_id')
        createIndex(tableName: 'devops_ci_template_pipeline', indexName: 'idx_source_type_source_id') {
            column(name: 'source_type')
            column(name: 'source_id')
        }
    }
    changeSet(author: 'wanghao', id: '2023-12-14-add-column') {
        addColumn(tableName: 'devops_ci_template_pipeline') {
            column(name: 'is_interruptible', type: 'TINYINT UNSIGNED', defaultValue: "0", remarks: '是否可中断', afterColumn: 'image')
        }
    }
}