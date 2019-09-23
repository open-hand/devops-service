import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import getTablePostData from '../../../../../../utils/getTablePostData';
import { useEnvironmentStore } from '../../../../stores';
import { RetryDataSet, GitopsLogDataSet, GitopsSyncDataSet } from './SyncDataSet';
import PermissionsDataSet from './PermissionsDataSet';
import ConfigDataSet from './ConfigDataSet';
import ConfigFormDataSet from './ConfigFormDataSet';
import BaseDataSet from './BaseDataSet';
import useStore from './useStore';

const Store = createContext();

export function useDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const prefixCls = 'c7ncd-deployment';
    const intlPrefix = 'c7ncd.deployment';
    const { intl: { formatMessage }, AppState: { currentMenuType: { id: projectId } }, children } = props;
    const { intlPrefix: currentIntlPrefix, envStore: { getSelectedMenu: { id } } } = useEnvironmentStore();

    const tabs = useMemo(() => ({
      SYNC_TAB: 'sync',
      CONFIG_TAB: 'config',
      ASSIGN_TAB: 'assign',
    }), []);
    const detailStore = useStore(tabs);
    const baseDs = useMemo(() => new DataSet(BaseDataSet()), []);
    const permissionsDs = useMemo(() => new DataSet(PermissionsDataSet({ formatMessage, intlPrefix })), []);
    const configDs = useMemo(() => new DataSet(ConfigDataSet({
      formatMessage,
      intlPrefix: currentIntlPrefix,
      projectId,
      id,
    })), [projectId, id]);
    const gitopsLogDs = useMemo(() => new DataSet(GitopsLogDataSet({ formatMessage, intlPrefix })), []);
    const gitopsSyncDs = useMemo(() => new DataSet(GitopsSyncDataSet()), []);
    const retryDs = useMemo(() => new DataSet(RetryDataSet()), []);
    const configFormDs = useMemo(() => new DataSet(ConfigFormDataSet({ formatMessage, intlPrefix, projectId, store: detailStore })), [projectId]);

    function queryData() {
      const tabKey = detailStore.getTabKey;
      switch (tabKey) {
        case tabs.SYNC_TAB: {
          retryDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/retry`;
          gitopsLogDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/error_file/page_by_env`;
          gitopsSyncDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/status`;
          gitopsSyncDs.query();
          gitopsLogDs.query();
          break;
        }
        case tabs.CONFIG_TAB:
          configDs.query();
          break;
        case tabs.ASSIGN_TAB: {
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
        }
        default:
      }
    }

    useEffect(() => {
      baseDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/info`;
      baseDs.query();
      queryData();
    }, [projectId, id, detailStore.getTabKey]);

    const value = {
      ...props,
      prefixCls,
      intlPrefix,
      tabs,
      baseDs,
      permissionsDs,
      configDs,
      gitopsLogDs,
      gitopsSyncDs,
      retryDs,
      detailStore,
      configFormDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
