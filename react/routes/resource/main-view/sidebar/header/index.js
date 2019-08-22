import React from 'react';
import { runInAction } from 'mobx';
import { Select } from 'choerodon-ui/pro';
import { useResourceStore } from '../../../stores';

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
    resourceStore,
  } = useResourceStore();

  function handleChoose(choose) {
    runInAction(() => {
      resourceStore.changeViewType(choose);
      resourceStore.setSelectedMenu({});
      resourceStore.setExpandedKeys([]);
      resourceStore.setSearchValue('');
    });
  }

  return <div className={`${prefixCls}-sidebar-head`}>
    <Select
      className={`${prefixCls}-sidebar-drop`}
      dropdownMatchSelectWidth
      onChange={handleChoose}
      value={resourceStore.getViewType}
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
