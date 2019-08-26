import React, { useContext, useMemo, useEffect, createContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro/lib';
import { useClusterStore } from '../../../../stores';
import NodePodsDataSet from './NodePodsDataSet';
import NodeInfoDataSet from './NodeInfoDataSet';


const NodeContentStore = createContext();

const useNodeContentStore = () => useContext(NodeContentStore);

const NodeContentStoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const { intl: { formatMessage }, AppState: { currentMenuType: { id: projectId } }, children } = props;
  const clusterStore = useClusterStore();
  const { parentId, name } = clusterStore.clusterStore.getSelectedMenu;
  const NodePodsDs = useMemo(() => new DataSet(NodePodsDataSet()), []);
  const NodeInfoDs = useMemo(() => new DataSet(NodeInfoDataSet()), []);
  
  useEffect(() => {
    NodeInfoDs.transport.read.url = `devops/v1/projects/${projectId}/clusters/nodes?cluster_id=${parentId}&node_name=${name}`;
    NodeInfoDs.query();
    NodePodsDs.transport.read.url = `devops/v1/projects/${projectId}/clusters/page_node_pods?cluster_id=${parentId}&node_name=${name}`;
    NodePodsDs.query();
  }, [projectId, name, parentId]);

  const value = {
    formatMessage,
    projectId,
    clusterStore: clusterStore.clusterStore,
    NodePodsDs,
    NodeInfoDs,
  };

  return (
    <NodeContentStore.Provider value={value}>
      {children}
    </NodeContentStore.Provider>
  );
})));

export {
  useNodeContentStore,
  NodeContentStoreProvider,
};
