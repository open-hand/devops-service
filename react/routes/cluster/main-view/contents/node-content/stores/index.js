import React, { useContext, useMemo, useEffect, createContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { axios } from '@choerodon/boot';
import { DataSet } from 'choerodon-ui/pro/lib';
import { useClusterStore } from '../../../../stores';
import NodePodsDataSet from './NodePodsDataSet';
import NodeInfoDataSet from './NodeInfoDataSet';
import useStore from './useStore';

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
  const tabs = useMemo(() => ({
    RESOURCE_TAB: 'resource',
    MONITOR_TAB: 'monitor',
  }), []);

  const nodePodsDs = useMemo(() => new DataSet(NodePodsDataSet()), []);
  const nodeInfoDs = useMemo(() => new DataSet(NodeInfoDataSet()), []);
  const contentStore = useStore(tabs);

  useEffect(() => {
    clusterStore.setNoHeader(true);
  }, []);

  const tabkey = contentStore.getTabKey;
  useEffect(() => {
    if (tabkey === tabs.RESOURCE_TAB) {
      nodeInfoDs.transport.read.url = `devops/v1/projects/${projectId}/clusters/nodes?cluster_id=${parentId}&node_name=${name}`;
      nodeInfoDs.query();
      nodePodsDs.transport.read.url = `devops/v1/projects/${projectId}/clusters/page_node_pods?cluster_id=${parentId}&node_name=${name}`;
      nodePodsDs.query();
    } else {
      loadMonitor();
    }
  }, [projectId, name, parentId, tabkey]);

  async function loadMonitor() {
    const res = await contentStore.loadGrafanaUrl(projectId, parentId);
    if (res) {
      const uri = escape(`${window.location}`);
      axios.get('/oauth/is_login', { withCredentials: true })
        .then((response) => {
          if (!response) {
            window.location = `${window._env_.API_HOST}/oauth/oauth/authorize?response_type=token&client_id=${window._env_.CLIENT_ID}&state=&redirect_uri=${uri}%26redirectFlag`;
          }
        });
    }
  }

  const value = {
    formatMessage,
    projectId,
    nodePodsDs,
    nodeInfoDs,
    tabs,
    contentStore,
    COMPONENT_TAB: 'component',
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
