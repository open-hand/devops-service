import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { getPrefixCls } from 'choerodon-ui/lib/configure';
import { useNodeContentStore } from './stores';

const NodeContent = observer((props) => {
  const {
    formatMessage,
    projectId,
    clusterStore,
  } = useNodeContentStore();
  const {
    prefixCls,
    intlPrefix,
    permissions,
    getSelectedMenu,
  } = clusterStore;
  
  return (
    <Fragment>
      <h1>Hello{getSelectedMenu}</h1>
    </Fragment>
  );
});

export default NodeContent;
