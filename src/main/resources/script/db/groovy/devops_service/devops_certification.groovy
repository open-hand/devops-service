package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'db/devops_certification.groovy') {
    changeSet(author: 'zhanglei', id: '2018-08-20-create-table') {
        createTable(tableName: "devops_certification", remarks: 'C7N Certification') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'name', type: 'VARCHAR(256)', remarks: '证书名字')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id')
            column(name: 'domains', type: 'VARCHAR(1024)', remarks: '域名')
            column(name: 'status', type: 'VARCHAR(16)', remarks: '状态')

            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


    changeSet(author: 'younger', id: '2018-09-10-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'command id', afterColumn: 'env_id')
        }
    }

    changeSet(author: 'Runge', id: '2018-09-10-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: 'valid_from', type: 'DATETIME', remarks: 'cert valid from', afterColumn: 'status')
            column(name: 'valid_until', type: 'DATETIME', remarks: 'cert valid until', afterColumn: 'valid_from')
        }
    }

    changeSet(author: 'Runge', id: '2018-09-11-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1", beforeColumn: 'created_by')
        }
    }

    changeSet(author: 'Runge', id: '2018-10-09-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: "certification_file_id", type: "BIGINT UNSIGNED", afterColumn: 'command_id')
        }
    }


    changeSet(author: 'Younger', id: '2018-12-10-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: "organization_id", type: "BIGINT UNSIGNED", afterColumn: 'env_id')
            column(name: 'org_cert_id', type: 'TINYINT UNSIGNED', afterColumn: 'organization_id')
            column(name: 'skip_check_project_permission', type: 'TINYINT UNSIGNED', remarks: '是否跳过项目权限校验', afterColumn: 'org_cert_id')
        }
    }

    changeSet(author: 'ZMF', id: '2019-08-12-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: "project_id", type: "BIGINT UNSIGNED", afterColumn: 'organization_id', remarks: '项目ID')
        }
    }

    changeSet(author: 'lihao', id: '2020-07-31-modify-column-org_cert_id') {
        modifyDataType(tableName: 'devops_certification', columnName: 'org_cert_id', newDataType: 'BIGINT UNSIGNED')
    }

    changeSet(author: 'zmf', id: '2021-03-25-add-api-version') {
        addColumn(tableName: 'devops_certification') {
            column(name: "api_version", type: "VARCHAR(128)", afterColumn: 'status', remarks: '证书资源的API版本')
        }
        sql("update devops_certification set api_version = 'certmanager.k8s.io/v1alpha1' where env_id is not null")
    }

    changeSet(author: 'lihao', id: '2023-03-27-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: 'advance_days', type: 'INT(1)', remarks: '提前通知时间 1 2 3')
            column(name: 'expire_notice', type: 'TINYINT(1)', remarks: '是否启用', defaultValue: '0')
        }
    }


    changeSet(author: 'lihao', id: '2023-03-28-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: 'type', type: 'VARCHAR(10)', remarks: '证书类型')
        }
    }

    changeSet(author: 'lihao', id: '2023-03-29-add-column') {
        addColumn(tableName: 'devops_certification') {
            column(name: 'notice_send_flag', type: 'TINYINT(1)', remarks: '证书过期通知发送标识 0 未发送 1 已发送', defaultValue: 0)
        }
    }

}