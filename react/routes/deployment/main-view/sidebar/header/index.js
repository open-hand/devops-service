import React, { useCallback } from 'react';
import { Select } from 'choerodon-ui/pro';
import { useDeploymentStore } from '../../../stores';
import { useSidebarStore } from '../stores';

import './index.less';

const { Option } = Select;

const SidebarHeader = () => {
  const {
    viewType: {
      IST_VIEW_TYPE,
      RES_VIEW_TYPE,
    },
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const { sidebarStore } = useSidebarStore();

  const handleChoose = useCallback((choose) => {
    deploymentStore.changeViewType(choose);
    sidebarStore.setExpandedKeys([]);
    sidebarStore.setSearchValue('');
  }, [deploymentStore, sidebarStore]);

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
