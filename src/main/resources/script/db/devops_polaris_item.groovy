package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_polaris_item.groovy') {
    changeSet(author: 'Zmf', id: '2020-02-14-create-table-polaris-item') {
        createTable(tableName: "devops_polaris_item", remarks: 'polaris扫描结果的检测项') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id / 可为空')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: '集群namespace') {
                constraints(nullable: false)
            }
            column(name: 'resource_name', type: 'VARCHAR(64)', remarks: '资源名称') {
                constraints(nullable: false)
            }
            column(name: 'resource_kind', type: 'VARCHAR(32)', remarks: '资源类型') {
                constraints(nullable: false)
            }
            column(name: 'severity', type: 'VARCHAR(25)', remarks: '重视程度, 忽视/警告/报错等') {
                constraints(nullable: false)
            }
            column(name: 'is_approved', type: 'TINYINT UNSIGNED', remarks: '是否通过此校验') {
                constraints(nullable: false)
            }
            column(name: 'record_id', type: 'BIGINT UNSIGNED', remarks: '扫描纪录id') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(45)', remarks: 'item的类型名称')
            column(name: 'category', type: 'VARCHAR(45)', remarks: '这个type所属的类型')
            column(name: 'message', type: 'VARCHAR(200)', remarks: '通过/未通过这一项时的message')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'zmf', id: '2020-02-19-add-polaris-item-idx') {
        createIndex(indexName: "idx_polaris_item_record_id ", tableName: "devops_polaris_item") {
            column(name: "record_id")
        }
    }
}