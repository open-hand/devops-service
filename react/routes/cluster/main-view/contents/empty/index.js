import React, { useEffect, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../components/header-buttons';
import { useClusterStore } from '../../../stores';
import CreateCluster from '../cluster-content/modals/create-cluster';
import { useClusterMainStore } from '../../stores';

const modalKey1 = Modal.key();

const EmptyPage = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);

  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    AppState: { currentMenuType: { id: projectId } },
    treeDs,
    clusterStore,
  } = useClusterStore();
  const { mainStore } = useClusterMainStore();
  function refreshTree() {
    treeDs.query();
  }

  useEffect(() => {
    clusterStore.setNoHeader(false);
  }, []);

  function openCreate() {
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.create` }),
      children: <CreateCluster
        afterOk={refreshTree}
        prefixCls={prefixCls}
        intlPrefix={intlPrefix}
        formatMessage={formatMessage}
        mainStore={mainStore}
        projectId={projectId}
      />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: 'create' }),
    });
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: `${intlPrefix}.modal.create` }),
      icon: 'playlist_add',
      handler: openCreate,
      display: true,
      group: 1,
    }];
  }

  return <HeaderButtons items={getButtons()} />;
});

export default EmptyPage;
