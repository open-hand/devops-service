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
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id: projectId } },
    children,
  } = props;
  const { clusterStore } = useClusterStore();
  const { getSelectedMenu: { parentId, name } } = clusterStore;
  const nodePodsDs = useMemo(() => new DataSet(NodePodsDataSet()), []);
  const nodeInfoDs = useMemo(() => new DataSet(NodeInfoDataSet()), []);

  useEffect(() => {
    clusterStore.setNoHeader(true);
  }, []);

  useEffect(() => {
    nodeInfoDs.transport.read.url = `devops/v1/projects/${projectId}/clusters/nodes?cluster_id=${parentId}&node_name=${name}`;
    nodeInfoDs.query();
    nodePodsDs.transport.read.url = `devops/v1/projects/${projectId}/clusters/page_node_pods?cluster_id=${parentId}&node_name=${name}`;
    nodePodsDs.query();
  }, [projectId, name, parentId]);

  const value = {
    formatMessage,
    projectId,
    nodePodsDs,
    nodeInfoDs,
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
