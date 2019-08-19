import React from 'react';
import { observer } from 'mobx-react-lite';

import { Breadcrumb } from '@choerodon/master';
import { useResourceStore } from '../stores';

import './index.less';

const DeploymentHeader = observer(() => {
  const {
    prefixCls,
    resourceStore: {
      getNoHeader,
    },
  } = useResourceStore();

  return <div className={`${prefixCls}-header`}>
    {!getNoHeader && <div className={`${prefixCls}-header-placeholder`} />}
    <Breadcrumb />
  </div>;
});

export default DeploymentHeader;
