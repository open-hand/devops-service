import React from 'react';
import { runInAction } from 'mobx';
import { Button } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';

import './index.less';

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
  const { getViewType } = resourceStore;

  function handleChoose(choose) {
    runInAction(() => {
      resourceStore.changeViewType(choose);
      resourceStore.setSelectedMenu({});
      resourceStore.setExpandedKeys([]);
      resourceStore.setSearchValue('');
    });
  }

  function chooseInstance() {
    getViewType !== IST_VIEW_TYPE && handleChoose(IST_VIEW_TYPE);
  }

  function chooseResource() {
    getViewType !== RES_VIEW_TYPE && handleChoose(RES_VIEW_TYPE);
  }

  return <div className={`${prefixCls}-sidebar-head`}>
    <Button
      type="primary"
      onClick={chooseInstance}
      className={getViewType === IST_VIEW_TYPE ? `${prefixCls}-sidebar-active` : ''}
    >
      {formatMessage({ id: `${intlPrefix}.viewer.${IST_VIEW_TYPE}` })}
    </Button>
    <Button
      type="primary"
      onClick={chooseResource}
      className={getViewType === RES_VIEW_TYPE ? `${prefixCls}-sidebar-active` : ''}
    >
      {formatMessage({ id: `${intlPrefix}.viewer.${RES_VIEW_TYPE}` })}
    </Button>
  </div>;
};

export default SidebarHeader;
