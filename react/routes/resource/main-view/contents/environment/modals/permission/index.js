import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { SelectBox, Select, Form } from 'choerodon-ui/pro';
import { handlePromptError } from '../../../../../../../utils';
import DynamicSelect from '../../../../../../../components/dynamic-select-new';
import UserInfo from '../../../../../../../components/userInfo';


const { Option } = Select;

export default observer((props) => {
  const { dataSet, nonePermissionDs, refresh, baseDs, store, projectId, formatMessage, prefixCls, intlPrefix, modal } = props;
  

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
  

  function renderUserOption({ record: optionRecord }) {
    return <UserInfo name={optionRecord.get('realName') || ''} id={record.get('loginName')} />;
  }
  

  function renderer({ optionRecord }) {
    return <UserInfo name={optionRecord.get('realName') || ''} id={record.get('loginName')} />;
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
          optionsRenderer={renderUserOption}
          optionsDataSet={nonePermissionDs}
          renderer={renderer}
          selectName="iamUserId"
          addText={formatMessage({ id: `${intlPrefix}.add.member` })}
        />
      )}
    </Fragment>
  );
});
