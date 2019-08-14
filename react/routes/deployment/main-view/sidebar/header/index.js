import React from 'react';
import { runInAction } from 'mobx';
import { Select } from 'choerodon-ui/pro';
import { useDeploymentStore } from '../../../stores';

import './index.less';

const { Option } = Select;

const SidebarHeader = () => {
  const {
    viewTypeMappings: {
      IST_VIEW_TYPE,
      RES_VIEW_TYPE,
    },
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();

  function handleChoose(choose) {
    runInAction(() => {
      deploymentStore.changeViewType(choose);
      deploymentStore.setSelectedMenu({});
      deploymentStore.setNoHeader(true);
      deploymentStore.setExpandedKeys([]);
      deploymentStore.setSearchValue('');
    });
  }

  return <div className={`${prefixCls}-sidebar-head`}>
    <Select
      className={`${prefixCls}-sidebar-drop`}
      dropdownMatchSelectWidth
      onChange={handleChoose}
      value={deploymentStore.getViewType}
      clearButton={false}
    >
      <Option value={IST_VIEW_TYPE} key={IST_VIEW_TYPE}>
        {formatMessage({ id: `${intlPrefix}.viewer.${IST_VIEW_TYPE}` })}
      </Option>,
      <Option value={RES_VIEW_TYPE} key={RES_VIEW_TYPE}>
        {formatMessage({ id: `${intlPrefix}.viewer.${RES_VIEW_TYPE}` })}
      </Option>,
    </Select>
  </div>;
};

export default SidebarHeader;
