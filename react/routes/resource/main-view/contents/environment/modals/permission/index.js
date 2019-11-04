import React, { Fragment, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import some from 'lodash/some';
import { SelectBox, Select, Form } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { handlePromptError } from '../../../../../../../utils';
import './index.less';

const { Option } = Select;

export default observer((props) => {
  const { dataSet, nonePermissionDs, refresh, record, store, projectId, formatMessage, prefixCls, intlPrefix, modal } = props;
  useEffect(() => {
    dataSet.getField('iamUserId').set('options', nonePermissionDs);
    nonePermissionDs.query();
  }, []);

  useEffect(() => {
    if (record.get('skipCheckPermission')) {
      dataSet.reset();
    } else {
      handleCreate();
    }
  }, [record.get('skipCheckPermission')]);

  modal.handleOk(async () => {
    const skipCheckPermission = record.get('skipCheckPermission');
    const baseData = {
      envId: record.get('id'),
      objectVersionNumber: record.get('objectVersionNumber'),
      skipCheckPermission,
    };
    if (skipCheckPermission) { 
      const res = await store.addUsers({
        projectId,
        userIds: [],
        ...baseData,
      });
      if (handlePromptError(res, false)) {
        refresh();
        return true;
      } else {
        return false;
      }
    }
    dataSet.transport.create = ({ data }) => {
      const res = {
        userIds: map(data, 'iamUserId'),
        ...baseData,
      };
      return {
        url: `/devops/v1/projects/${projectId}/envs/${record.get('id')}/permission`,
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
          <Option value>{formatMessage({ id: `${intlPrefix}.member.all` })}</Option>
          <Option value={false}>{formatMessage({ id: `${intlPrefix}.member.specific` })}</Option>
        </SelectBox>
      </Form>
      {!record.get('skipCheckPermission') && (
        <Fragment>
          {map(dataSet.created, (userRecord, index) => (
            <div className={`${prefixCls}-permission-form-item`} key={`permission-form-${index}`}>
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
            {formatMessage({ id: `${intlPrefix}.add.member` })}
          </Button>
        </Fragment>
      )}
    </div>

  );
});
