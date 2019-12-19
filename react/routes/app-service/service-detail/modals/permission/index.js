import React, { Fragment, useEffect, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import compact from 'lodash/compact';
import { SelectBox, Select, Form } from 'choerodon-ui/pro';
import DynamicSelect from '../../../../../components/dynamic-select-new';
import UserInfo from '../../../../../components/userInfo';


import './index.less';

const { Option } = Select;

export default observer((props) => {
  const { dataSet, nonePermissionDs, refresh, baseDs, store, projectId, formatMessage, prefixCls, intlPrefix, modal } = props;

  const record = useMemo(() => baseDs.current, [baseDs.current]);

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
          userIds: compact(map(data, 'iamUserId') || []),
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
