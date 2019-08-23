import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useClusterStore } from '../../../../stores';
import useStore from './useStore';
import NodeListDataSet from './NodeListDataSet';
import PermissionDataSet from './PermissionDataSet';
import ClusterDetailDataSet from './ClusterDetailDataSet';
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
    }), []);
    const { intl: { formatMessage }, AppState: { currentMenuType: { id } }, children } = props;
    const { intlPrefix, clusterStore } = useClusterStore();
    const { getSelectedMenu: { menuId } } = clusterStore;
    const NodeListDs = useMemo(() => new DataSet(NodeListDataSet({ formatMessage, intlPrefix, id })), []);
    const PermissionDs = useMemo(() => new DataSet(PermissionDataSet({ formatMessage, intlPrefix, id })), []);
    const ClusterDetailDs = useMemo(() => new DataSet(ClusterDetailDataSet({ formatMessage, intlPrefix, id })), []);
    const contentStore = useStore(tabs);
    const tabkey = contentStore.getTabKey;
    useEffect(() => {
      if (tabkey === tabs.NODE_TAB) {
        NodeListDs.transport.read.url = `/devops/v1/projects/${id}/clusters/page_nodes?cluster_id=${menuId}`;
        NodeListDs.query();
      } else {
        PermissionDs.transport.read = ({ data }) => {
          const postData = getTablePostData(data);
          return {
            url: `/devops/v1/projects/${id}/clusters/${menuId}/permission/page_related`,
            method: 'post',
            data: postData,
          };
        };
        PermissionDs.transport.destroy = {
          url: `/devops/v1/projects/${id}/clusters/${menuId}/permission`,
          method: 'delete',
        };
        PermissionDs.query();
      }
    }, [id, menuId, tabkey]);
    useEffect(() => {
      ClusterDetailDs.transport.read.url = `devops/v1/projects/${id}/clusters/${menuId}`;
      ClusterDetailDs.query();
    }, [id, menuId]);

    const value = {
      ...props,
      tabs,
      contentStore,
      NodeListDs,
      PermissionDs,
      intlPrefix,
      formatMessage,
      ClusterDetailDs,
      projectId: id,
      clusterId: menuId,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
