import React, { useEffect, createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import { viewTypeMappings, itemTypeMappings } from './mappings';
import TreeDataSet from './TreeDataSet';

const { CLU_VIEW_TYPE } = viewTypeMappings;
const Store = createContext();

export function useClusterStore() {
  return useContext(Store);
}


export const StoreProvider = injectIntl(inject('AppState')(observer(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const clusterStore = useStore();
    const viewType = clusterStore.getViewType;
    const viewTypeMemo = useMemo(() => viewTypeMappings, []);
    const itemType = useMemo(() => itemTypeMappings, []);
    const treeDs = useMemo(() => new DataSet(TreeDataSet(clusterStore, viewType)), [viewType]);
    useEffect(() => {
      const urlMaps = {
        [CLU_VIEW_TYPE]: `/devops/v1/projects/${id}/clusters/tree_menu`,
      };
      treeDs.transport.read.url = urlMaps[viewType];
      treeDs.query();
    }, [id, viewType]);
    const value = {
      ...props,
      prefixCls: 'c7ncd-cluster',
      intlPrefix: 'c7ncd.cluster',
      permissions: [
        'devops-service.devops-cluster.queryClustersAndNodes',
        'devops-service.devops-cluster.query',
        'devops-service.project-certification.listAllNonRelatedMembers',
        'devops-service.devops-cluster.listClusterNodes',
        'devops-service.project-certification.pageRelatedProjects',
        'devops-service.devops-cluster.assignPermission',
        'devops-service.devops-cluster.checkName',
        'devops-service.devops-cluster.checkCode',
        'devops-service.devops-cluster.create',
        'devops-service.devops-cluster.update',
        'devops-service.devops-cluster.queryShell',
        'devops-service.devops-cluster.deleteCluster',
        'devops-service.devops-cluster.queryNodeInfo',
        'devops-service.devops-cluster.pageQueryPodsByNodeName',
      ],
      clusterStore,
      itemType,
      viewTypeMemo,
      viewType,
      treeDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  } 
)));
