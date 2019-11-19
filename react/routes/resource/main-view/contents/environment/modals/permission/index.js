import React, { Fragment, useEffect, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import some from 'lodash/some';
import { SelectBox, Select, Form, Tooltip } from 'choerodon-ui/pro';
import { handlePromptError } from '../../../../../../../utils';
import DynamicSelect from '../../../../../../../components/dynamic-select-new';

const { Option } = Select;

export default observer((props) => {
  const { dataSet, nonePermissionDs, refresh, baseDs, store, projectId, formatMessage, prefixCls, intlPrefix, modal } = props;
  useEffect(() => {
    dataSet.getField('iamUserId').set('options', nonePermissionDs);
    nonePermissionDs.query();
  }, []);

  const record = useMemo(() => baseDs.current, [baseDs.current]);

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

  function handleUserFilter(optionRecord) {
    const flag = some(dataSet.created, (creatRecord) => creatRecord.get('iamUserId') === optionRecord.get('iamUserId'));
    return !flag;
  }

  function renderUserOption({ record: optionRecord }) {
    return <Tooltip title={optionRecord.get('loginName')}>{optionRecord.get('realName')}</Tooltip>;
  }

  return (
    <Fragment>
      <Form record={record}>
        <SelectBox name="skipCheckPermission">
          <Option value>{formatMessage({ id: `${intlPrefix}.member.all` })}</Option>
          <Option value={false}>{formatMessage({ id: `${intlPrefix}.member.specific` })}</Option>
        </SelectBox>
      </Form>
      {record && !record.get('skipCheckPermission') && (
        <DynamicSelect
          selectDataSet={dataSet} 
          optionsFilter={handleUserFilter} 
          optionsRenderer={renderUserOption}
          selectName="iamUserId"
          addText={formatMessage({ id: `${intlPrefix}.add.member` })}
        />
      )}
    </Fragment>
  );
});
