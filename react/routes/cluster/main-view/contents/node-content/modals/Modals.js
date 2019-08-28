import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useClusterStore } from '../../../../stores';
import { useNodeContentStore } from '../stores';

const modalKey1 = Modal.key();
const ClusterNodeModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    clusterStore,
    AppState: { currentMenuType: { id: projectId } },
    treeDs,
  } = useClusterStore();
  
  const {
    NodePodsDs,
    NodeInfoDs,
  } = useNodeContentStore();

  const { menuId } = clusterStore.getSelectedMenu;


  function refresh() {
    NodePodsDs.query();
    NodeInfoDs.query();
  }
  

  function getButtons() {
    return [{
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
    }];
  }

  return <HeaderButtons items={getButtons()} />;
});

export default ClusterNodeModals;
