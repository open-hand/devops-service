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
import ConfigDataSet from './ConfigDataSet';
import ConfigFormDataSet from './ConfigFormDataSet';

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
      CONFIG_TAB: 'config',
      ASSIGN_TAB: 'assign',
    }), []);
    const envStore = useStore({ defaultTab: tabs.SYNC_TAB });
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
    const permissionsDs = useMemo(() => new DataSet(PermissionsDataSet({ formatMessage, intlPrefix })), []);
    const gitopsLogDs = useMemo(() => new DataSet(GitopsLogDataSet({ formatMessage, intlPrefix })), []);
    const gitopsSyncDs = useMemo(() => new DataSet(GitopsSyncDataSet()), []);
    const retryDs = useMemo(() => new DataSet(RetryDataSet()), []);
    const configDs = useMemo(() => new DataSet(ConfigDataSet({ formatMessage, intlPrefix })), []);
    const configFormDs = useMemo(() => new DataSet(ConfigFormDataSet({ formatMessage, intlPrefix, projectId, store: envStore })), [projectId]);


    function queryData() {
      const tabKey = envStore.getTabKey;
      switch (tabKey) {
        case tabs.SYNC_TAB:
          retryDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/retry`;
          gitopsLogDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/error_file/page_by_env`;
          gitopsSyncDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/status`;
          gitopsSyncDs.query();
          gitopsLogDs.query();
          break;
        case tabs.CONFIG_TAB:
          configDs.transport.destroy = ({ data: [data] }) => ({
            url: `/devops/v1/projects/${projectId}/deploy_value?value_id=${data.id}`,
            method: 'delete',
            data: null,
          });
          configDs.transport.read = ({ data }) => {
            const postData = getTablePostData(data);
            return {
              url: `/devops/v1/projects/${projectId}/deploy_value/page_by_options?env_id=${id}`,
              method: 'post',
              data: postData,
            };
          };
          configDs.query();
          break;
        case tabs.ASSIGN_TAB:
          permissionsDs.transport.destroy = ({ data: [data] }) => ({
            url: `/devops/v1/projects/${projectId}/envs/${id}/permission?user_id=${data.iamUserId}`,
            method: 'delete',
          });
          permissionsDs.transport.read = ({ data }) => {
            const postData = getTablePostData(data);
            return {
              url: `/devops/v1/projects/${projectId}/envs/${id}/permission/page_by_options`,
              method: 'post',
              data: postData,
            };
          };
          permissionsDs.query();
          break;
        default:
      }
    }

    useEffect(() => {
      baseInfoDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/info`;
      baseInfoDs.query();
      queryData();
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
      configDs,
      configFormDs,
      envStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
