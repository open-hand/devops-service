import React from 'react';
import { observer } from 'mobx-react-lite';

import { Breadcrumb } from '@choerodon/master';
import { useDeploymentStore } from '../stores';

import './index.less';

const DeploymentHeader = observer(() => {
  const {
    prefixCls,
    resourceStore: {
      getSelectedMenu: { menuType },
      getNoHeader,
    },
  } = useResourceStore();

  return <div className={`${prefixCls}-header`}>
    {!getNoHeader && <div className={`${prefixCls}-header-placeholder`} />}
    <Breadcrumb title={menuType} />
  </div>;
});

export default DeploymentHeader;
