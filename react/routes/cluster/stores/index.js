import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import { itemTypeMappings } from './mappings';
import TreeDataSet from './TreeDataSet';

const Store = createContext();

export function useClusterStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const clusterStore = useStore();
    const itemType = useMemo(() => itemTypeMappings, []);
    const treeDs = useMemo(() => new DataSet(TreeDataSet(clusterStore, id)), [id]);

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
      treeDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
