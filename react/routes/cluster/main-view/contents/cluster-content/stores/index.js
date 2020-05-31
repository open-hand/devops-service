import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { axios } from '@choerodon/boot';
import { DataSet } from 'choerodon-ui/pro';
import { useClusterStore } from '../../../../stores';
import { useClusterMainStore } from '../../../stores';
import useStore from './useStore';
import NodeListDataSet from './NodeListDataSet';
import PermissionDataSet from './PermissionDataSet';
import PolarisNumDataSet from './PalarisNumDataSet';

import getTablePostData from '../../../../../../utils/getTablePostData';
import SummaryDataSet from './SummaryDataSet';
import EnvDetailDataSet from './EnvDetailDataSet';

const Store = createContext();


export function useClusterContentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const tabs = useMemo(() => ({
      NODE_TAB: 'node',
      POLARIS_TAB: 'polaris',
      ASSIGN_TAB: 'assign',
      COMPONENT_TAB: 'component',
      MONITOR_TAB: 'monitor',
    }), []);
    const { intl: { formatMessage }, AppState: { currentMenuType: { id: projectId } }, children } = props;
    const { ClusterDetailDs, mainStore } = useClusterMainStore();
    const { intlPrefix, clusterStore } = useClusterStore();
    const { getSelectedMenu: { id } } = clusterStore;

    useEffect(() => {
      clusterStore.setNoHeader(false);
    }, []);

    useEffect(() => {
      if (mainStore.getClusterDefaultTab) {
        contentStore.setTabKey(mainStore.getClusterDefaultTab);
        mainStore.setClusterDefaultTab(null);
      }
    }, [mainStore.getClusterDefaultTab]);

    const record = ClusterDetailDs.current;
    const NodeListDs = useMemo(() => new DataSet(NodeListDataSet({ formatMessage, intlPrefix })), []);
    const PermissionDs = useMemo(() => new DataSet(PermissionDataSet({ formatMessage, intlPrefix, projectId, id, skipCheckProjectPermission: record && record.get('skipCheckProjectPermission') })), [record]);
    const clusterSummaryDs = useMemo(() => new DataSet(SummaryDataSet()), []);
    const envDetailDs = useMemo(() => new DataSet(EnvDetailDataSet()), []);

    const polarisNumDS = useMemo(() => new DataSet(PolarisNumDataSet({ formatMessage, intlPrefix, projectId, id })), [record]);

    const contentStore = useStore(tabs);
    let URL = '';
    const tabkey = contentStore.getTabKey;
    useEffect(() => {
      switch (tabkey) {
        case tabs.NODE_TAB:
          NodeListDs.transport.read.url = `/devops/v1/projects/${projectId}/clusters/page_nodes?cluster_id=${id}`;
          NodeListDs.query();
          break;
        case tabs.ASSIGN_TAB:
          if (!record) {
            return;
          }
          if (record.get('skipCheckProjectPermission')) {
            URL = `/devops/v1/projects/${projectId}/page_projects`;
          } else {
            URL = `/devops/v1/projects/${projectId}/clusters/${id}/permission/page_related`;
          }
          PermissionDs.transport.read = ({ data }) => {
            const postData = getTablePostData(data);
            return {
              url: URL,
              method: 'post',
              data: postData,
            };
          };
          PermissionDs.transport.destroy = {
            url: `/devops/v1/projects/${projectId}/clusters/${id}/permission`,
            method: 'delete',
          };
          PermissionDs.query();
          break;
        case tabs.COMPONENT_TAB:
          contentStore.loadComponentList(projectId, id);
          break;
        case tabs.MONITOR_TAB:
          loadMonitor();
          break;
        case tabs.POLARIS_TAB:
          clusterSummaryDs.transport.read.url = `/devops/v1/projects/${projectId}/polaris/clusters/${id}/summary`;
          envDetailDs.transport.read.url = `/devops/v1/projects/${projectId}/polaris/clusters/${id}/env_detail`;
          polarisNumDS.transport.read.url = `devops/v1/projects/${projectId}/polaris/records?scope=cluster&scope_id=${id}`;
          loadPolaris();
          break;
        default:
      }
    }, [projectId, tabkey, record]);

    async function loadPolaris() {
      const res = await contentStore.checkHasEnv(projectId, id);
      if (res) {
        polarisNumDS.query();
        clusterSummaryDs.query();
        envDetailDs.query();
      }
    }
    async function loadMonitor() {
      const res = await contentStore.loadGrafanaUrl(projectId, id);
      if (res) {
        const uri = escape(`${window.location}`);
        axios.get('oauth/public/is-online', { withCredentials: true })
          .then((response) => {
            if (!response) {
              window.location = `${window._env_.API_HOST}/oauth/oauth/authorize?response_type=token&client_id=${window._env_.CLIENT_ID}&state=&redirect_uri=${uri}%26redirectFlag`;
            }
          });
      }
    }

    const value = {
      ...props,
      tabs,
      contentStore,
      NodeListDs,
      PermissionDs,
      intlPrefix,
      formatMessage,
      ClusterDetailDs,
      polarisNumDS,
      projectId,
      clusterId: id,
      clusterSummaryDs,
      envDetailDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
