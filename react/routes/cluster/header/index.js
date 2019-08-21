import React from 'react';
import { observer } from 'mobx-react-lite';
import { Breadcrumb } from '@choerodon/master';
import { useClusterStore } from '../stores';

import './index.less';

const ClusterHeader = observer(() => {
  const {
    prefixCls,
  } = useClusterStore();

  return <div className={`${prefixCls}-header`}>
    {<div className={`${prefixCls}-header-placeholder`} />}
    <Breadcrumb />
  </div>;
});

export default ClusterHeader;
