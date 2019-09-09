import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import getTablePostData from '../../../../../../utils/getTablePostData';
import BaseInfoDataSet from './BaseInfoDataSet';
import PermissionsDataSet from './PermissionsDataSet';
import GitopsLogDataSet from './GitopsLogDataSet';
import GitopsSyncDataSet from './GitopsSyncDataSet';
import RetryDataSet from './RetryDataSet';
import { useResourceStore } from '../../../../stores';
import useStore from './useStore';

const Store = createContext();

export function useEnvironmentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      intl: { formatMessage },
      AppState: { currentMenuType: { id: projectId, organizationId, type: resourceType } },
      children,
    } = props;
    const {
      intlPrefix,
      resourceStore: { getSelectedMenu: { id } },
    } = useResourceStore();

    const tabs = useMemo(() => ({
      SYNC_TAB: 'sync',
      ASSIGN_TAB: 'assign',
    }), []);
    const envStore = useStore({ defaultTab: tabs.SYNC_TAB });
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
    const permissionsDs = useMemo(() => new DataSet(PermissionsDataSet({ formatMessage, intlPrefix })), [projectId, id]);
    const gitopsLogDs = useMemo(() => new DataSet(GitopsLogDataSet({ formatMessage, intlPrefix })), []);
    const gitopsSyncDs = useMemo(() => new DataSet(GitopsSyncDataSet()), []);
    const retryDs = useMemo(() => new DataSet(RetryDataSet()), []);

    useEffect(() => {
      retryDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/retry`;
      baseInfoDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/info`;
      baseInfoDs.query();
      permissionsDs.transport.read = ({ data }) => {
        const postData = getTablePostData(data);
        return {
          url: `/devops/v1/projects/${projectId}/envs/${id}/permission/page_by_options`,
          method: 'post',
          data: postData,
        };
      };
      permissionsDs.query();
      permissionsDs.transport.destroy = ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/envs/${id}/permission?user_id=${data.iamUserId}`,
        method: 'delete',
      });
    }, [projectId, id]);

    useEffect(() => {
      const tabKey = envStore.getTabKey;
      if (tabKey === tabs.SYNC_TAB) {
        gitopsSyncDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/status`;
        gitopsSyncDs.query();
        gitopsLogDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/error_file/page_by_env`;
        gitopsLogDs.query();
      } else if (tabKey === tabs.ASSIGN_TAB) {
        permissionsDs.query();
      }
    }, [projectId, id, envStore.getTabKey]);

    useEffect(() => {
      envStore.checkPermission({ projectId, organizationId, resourceType });
    }, [projectId, organizationId, resourceType]);

    const value = {
      ...props,
      tabs,
      baseInfoDs,
      permissionsDs,
      gitopsLogDs,
      gitopsSyncDs,
      retryDs,
      envStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
