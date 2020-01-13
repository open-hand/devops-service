import React, { Fragment, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import { map, some, compact } from 'lodash';
import { SelectBox, Select, Form, Tooltip } from 'choerodon-ui/pro';
import DynamicSelect from '../../../../../../../components/dynamic-select-new';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const { Option } = Select;

const Permission = observer((props) => {
  const { refreshPermission, modal, onOk, intlPrefix, prefixCls, formatMessage, clusterDetail, PermissionDs, NonPermissionDs } = props;

  useEffect(() => () => {
    clusterDetail.reset();
  }, []);
  
  modal.handleOk(async () => {
    const projectIds = map(PermissionDs.created, (createdRecord) => createdRecord.get('projectId')) || [];
    const skipCheckProjectPermission = clusterDetail.get('skipCheckProjectPermission');
    const projects = {
      projectIds: compact(projectIds),
      skipCheckProjectPermission,
    };
    if (!(projects && projects.projectIds)) return false;
    try {
      const res = await onOk(projects);
      if (handlePromptError(res, false)) {
        refreshPermission();
        return true;
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });
  

  function handleFilter(optionRecord) {
    const flag = some(PermissionDs.created, (creatRecord) => creatRecord.get('projectId') === optionRecord.get('id'));
    return !flag;
  }

  function renderOption({ record: optionRecord }) {
    const code = optionRecord.get('code');
    const name = optionRecord.get('name');
    return <Tooltip title={code}>{name}</Tooltip>;
  }

  return (
    <Fragment>
      <div className={`${prefixCls}-modal-head`}>{formatMessage({ id: `${intlPrefix}.visibility` })}</div>
      <Form record={clusterDetail}>
        <SelectBox name="skipCheckProjectPermission">
          <Option value>{formatMessage({ id: `${intlPrefix}.project.all` })}</Option>
          <Option value={false}>{formatMessage({ id: `${intlPrefix}.project.part` })}</Option>
        </SelectBox>
      </Form>
      {!clusterDetail.get('skipCheckProjectPermission') && (
        <DynamicSelect
          selectDataSet={PermissionDs}
          optionsDataSet={NonPermissionDs}
          optionsFilter={handleFilter} 
          optionsRenderer={renderOption}
          selectName="projectId"
          optionKeyName="id"
          addText={formatMessage({ id: `${intlPrefix}.add.project` })}
        />
      )}
    </Fragment>
  );
});

export default Permission;
