import React, { Fragment, useEffect } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import some from 'lodash/some';
import { SelectBox, Select, Form } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';

import './index.less';

const { Option } = Select;

export default observer(({ dataSet, refresh, record, store, projectId, formatMessage, prefixCls, intlPrefix, nonePermissionDS, modal }) => {
  useEffect(() => {
    dataSet.getField('iamUserId').set('options', nonePermissionDS);
    nonePermissionDS.query();
  }, []);

  useEffect(() => {
    if (record.get('skipCheckPermission')) {
      dataSet.reset();
    } else {
      handleCreate();
    }
  }, [record.get('skipCheckPermission')]);

  modal.handleOk(async () => {
    if (record.get('skipCheckPermission')) {
      try {
        const data = {
          skipCheckPermission: true,
          userIds: [],
        };
        if (await store.updatePermission(projectId, record.get('id'), data)) {
          refresh();
        } else {
          return false;
        }
      } catch (e) {
        return false;
      }
    } else {
      dataSet.transport.create = ({ data }) => {
        const res = {
          skipCheckPermission: false,
          userIds: map(data, 'iamUserId'),
        };
        return {
          url: `/devops/v1/projects/${projectId}/app_service/${record.get('id')}/update_permission`,
          method: 'post',
          data: res,
        };
      };
      try {
        if (await dataSet.submit() !== false) {
          refresh();
        } else {
          return false;
        }
      } catch (e) {
        return false;
      }
    }
  });

  modal.handleCancel(() => {
    record.reset();
    dataSet.reset();
  });

  function handleDelete(current) {
    dataSet.remove(current);
  }

  function handleCreate() {
    dataSet.create();
  }

  function handleUserFilter(optionRecord) {
    const flag = some(dataSet.created, (creatRecord) => creatRecord.get('iamUserId') === optionRecord.get('iamUserId'));
    return !flag;
  }

  function renderUserOption({ record: optionRecord }) {
    return <Tooltip title={optionRecord.get('loginName')}>{optionRecord.get('realName')}</Tooltip>;
  }

  return (
    <div className={`${prefixCls}-permission-form`}>
      <Form record={record}>
        <SelectBox name="skipCheckPermission">
          <Option value>{formatMessage({ id: `${intlPrefix}.user.all` })}</Option>
          <Option value={false}>{formatMessage({ id: `${intlPrefix}.user.some` })}</Option>
        </SelectBox>
      </Form>
      {!record.get('skipCheckPermission') && (
        <Fragment>
          {map(dataSet.created, (userRecord) => (
            <div className={`${prefixCls}-permission-form-item`}>
              <Form record={userRecord}>
                <Select name="iamUserId" optionsFilter={handleUserFilter} searchable optionRenderer={renderUserOption} />
              </Form>
              <Button
                icon="delete"
                shape="circle"
                onClick={() => handleDelete(userRecord)}
                disabled={dataSet.created.length === 1}
                className={`${prefixCls}-permission-form-button`}
              />
            </div>
          ))}
          <Button
            icon="add"
            type="primary"
            onClick={handleCreate}
          >
            <FormattedMessage id={`${intlPrefix}.add.mbr`} />
          </Button>
        </Fragment>
      )}
    </div>

  );
});
