import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useClusterStore } from '../../../../stores';
import { useClusterMainStore } from '../../../stores';
import useStore from './useStore';
import NodeListDataSet from './NodeListDataSet';
import PermissionDataSet from './PermissionDataSet';
import getTablePostData from '../../../../../../utils/getTablePostData';

const Store = createContext();


export function useClusterContentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const tabs = useMemo(() => ({
      NODE_TAB: 'node',
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
          contentStore.loadPrometheusStatus(projectId, id);
          break;
        case tabs.MONITOR_TAB:
          contentStore.loadGrafanaUrl(projectId, id);
          break;
        default:
      }
    }, [projectId, id, tabkey, record]);

    const value = {
      ...props,
      tabs,
      contentStore,
      NodeListDs,
      PermissionDs,
      intlPrefix,
      formatMessage,
      ClusterDetailDs,
      projectId,
      clusterId: id,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
