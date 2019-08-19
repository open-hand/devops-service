import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
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
    const { intl: { formatMessage }, AppState: { currentMenuType: { id } }, children } = props;
    const { intlPrefix, resourceStore } = useResourceStore();
    const { getSelectedMenu: { menuId } } = resourceStore;

    const tabs = useMemo(() => ({
      SYNC_TAB: 'sync',
      ASSIGN_TAB: 'assign',
    }), []);
    const envStore = useStore(tabs);
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
    const permissionsDs = useMemo(() => new DataSet(PermissionsDataSet({
      formatMessage,
      intlPrefix,
      projectId: id,
      id: menuId,
    })), [id, menuId]);
    const gitopsLogDs = useMemo(() => new DataSet(GitopsLogDataSet({ formatMessage, intlPrefix })), []);
    const gitopsSyncDs = useMemo(() => new DataSet(GitopsSyncDataSet()), []);
    const retryDs = useMemo(() => new DataSet(RetryDataSet()), []);


    useEffect(() => {
      retryDs.transport.read.url = `/devops/v1/projects/${id}/envs/${menuId}/retry`;
      baseInfoDs.transport.read.url = `/devops/v1/projects/${id}/envs/${menuId}/info`;
      baseInfoDs.query();
    }, [id, menuId]);

    const tabKey = envStore.getTabKey;

    useEffect(() => {
      if (tabKey === tabs.SYNC_TAB) {
        gitopsSyncDs.transport.read.url = `/devops/v1/projects/${id}/envs/${menuId}/status`;
        gitopsSyncDs.query();
        gitopsLogDs.transport.read.url = `/devops/v1/projects/${id}/envs/${menuId}/error_file/page_by_env`;
        gitopsLogDs.query();
      } else if (tabKey === tabs.ASSIGN_TAB) {
        permissionsDs.query();
      }
    }, [id, menuId, tabKey]);

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
