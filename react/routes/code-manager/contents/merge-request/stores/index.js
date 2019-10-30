import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import DevPipelineStore from '../../../stores/DevPipelineStore';
import OpenTableDataSet from './OpenTableDataSet';
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

  const appId = DevPipelineStore.getSelectApp;
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
    openTableDS.transport.read.url = url;
    openTableDS.paging = tabKey !== 'opened';
    appId && mergedRequestStore.loadUrl(id, appId);
  }, [appId, tabKey, projectId]);

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}))));

export default StoreProvider;
