package script.db

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

    changeSet(author: 'younger', id: '2018-11-22-add-column') {
        addColumn(tableName: 'devops_application') {
            column(name: 'type', type: 'VARCHAR(50)', remarks: '应用类型', afterColumn: 'code')
        }
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
}