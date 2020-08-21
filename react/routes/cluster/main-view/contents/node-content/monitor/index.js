import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { useClusterMainStore } from '../../../stores';
import { useNodeContentStore } from '../stores';
import { useClusterStore } from '../../../../stores';
import Loading from '../../../../../../components/loading';
import EmptyPage from '../../../../../../components/empty-page';
import setTreeMenuSelect from '../../../../../../utils/setTreeMenuSelect';

import './index.less';

export default observer((props) => {
  const {
    clusterStore,
    treeDs,
  } = useClusterStore();
  const {
    intlPrefix,
    prefixCls,
    mainStore,
  } = useClusterMainStore();
  const {
    contentStore: {
      getGrafanaUrl,
    },
    formatMessage,
    COMPONENT_TAB,
  } = useNodeContentStore();
  const {
    getSelectedMenu: { name, parentId, key },
    setSelectedMenu,
  } = clusterStore;

  function refresh() {

  }

  function LinkToComponent() {
    const clusterRecord = treeDs.find((record) => record.get('id') === parentId);
    const currentRecord = treeDs.find((record) => record.get('key') === key);
    if (clusterRecord) {
      mainStore.setClusterDefaultTab(COMPONENT_TAB);
    }
    currentRecord.isSelected = false;
    setSelectedMenu({ ...clusterRecord.toData() });
    setTreeMenuSelect(treeDs, clusterStore);
  }

  function getContent() {
    if (getGrafanaUrl) {
      const clusterRecord = treeDs.find((record) => record.get('id') === Number(parentId));
      const code = clusterRecord && clusterRecord.get('code');
      return (
        <iframe
          height={700}
          width="100%"
          src={`${getGrafanaUrl}?kiosk=tv&var-node=${name}`}
          title="grafanaNode"
          frameBorder={0}
          sandbox
        />
      );
    } else {
      return (
        <EmptyPage
          title={formatMessage({ id: `${intlPrefix}.monitor.install` })}
          describe={formatMessage({ id: `${intlPrefix}.monitor.empty.des` })}
          btnText={formatMessage({ id: `${intlPrefix}.monitor.link` })}
          onClick={LinkToComponent}
          access
        />
      );
    }
  }

  return (
    <div className={`${prefixCls}-node-monitor-wrap`}>
      {getContent()}
    </div>
  );
});
