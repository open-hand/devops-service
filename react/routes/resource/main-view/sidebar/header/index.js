import React from 'react';
import { runInAction } from 'mobx';
import { SelectBox, Icon } from 'choerodon-ui/pro';
import { useResourceStore } from '../../../stores';

import './index.less';

const { Option } = SelectBox;

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
    <SelectBox
      mode="button"
      className={`${prefixCls}-sidebar-box`}
      onChange={handleChoose}
      value={resourceStore.getViewType}
    >
      <Option value={IST_VIEW_TYPE} key={IST_VIEW_TYPE}>
        <Icon type="instance_outline" />
        <span className={`${prefixCls}-sidebar-option`}>{formatMessage({ id: `${intlPrefix}.viewer.${IST_VIEW_TYPE}` })}</span>
      </Option>,
      <Option value={RES_VIEW_TYPE} key={RES_VIEW_TYPE}>
        <Icon type="folder_open" />
        <span className={`${prefixCls}-sidebar-option`}>{formatMessage({ id: `${intlPrefix}.viewer.${RES_VIEW_TYPE}` })}</span>
      </Option>,
    </SelectBox>
  </div>;
};

export default SidebarHeader;
