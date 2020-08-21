import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import OpenTableDataSet from './OpenTableDataSet';
import { useCodeManagerStore } from '../../../stores';
import useStore from './useStore';

const Store = createContext();
export function useRequestStore() {
  return useContext(Store);
}

const StoreProvider = injectIntl(inject('AppState')(observer(((props) => {
  const {
    children,
    AppState: { currentMenuType: { id: projectId, name, id } },
    intl: { formatMessage },
  } = props;

  const mergedRequestStore = useStore();

  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  const appId = selectAppDs.current.get('appServiceId');

  const tabKey = mergedRequestStore.getTabKey;

  const openTableDS = useMemo(() => new DataSet(OpenTableDataSet(projectId, formatMessage, mergedRequestStore, appId, tabKey)), []);

  const value = {
    ...props,
    openTableDS,
    mergedRequestStore,
  };

  useEffect(() => {
    let url;
    if (tabKey !== 'all') {
      url = `/devops/v1/projects/${projectId}/app_service/${appId}/git/list_merge_request?state=${tabKey}`;
    } else {
      url = `/devops/v1/projects/${projectId}/app_service/${appId}/git/list_merge_request`;
    }
    if (appId) { openTableDS.transport.read.url = url; }
  }, [appId, tabKey, projectId]);

  useEffect(() => {
    appId && mergedRequestStore.loadUrl(id, appId);
  }, [appId, projectId]);

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}))));

export default StoreProvider;
