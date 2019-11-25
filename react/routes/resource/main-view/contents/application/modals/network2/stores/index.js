import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';

import { DataSet } from 'choerodon-ui/pro';
import CreateFormDataSet from './CreateFormDataSet';
import PortDataSet from './PortDataSet';
import AppInstanceOptionsDataSet from './AppInstanceOptionsDataSet';
import KeyOptionsDataSet from './KeyOptionsDataSet';
import TargetLabelsDataSet from './TargetLabelsDataSet';

const NetWorkStore = createContext();

function useNetWorkStore() {
  return useContext(NetWorkStore);
}

function StoreProvider(props) {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    children,
    envId,
    appId,
    networkStore,
  } = props;

  const appInstanceOptionsDs = useMemo(() => new DataSet(AppInstanceOptionsDataSet(projectId, envId, appId, formatMessage)), [projectId, envId, appId]);
  const keyOptionsDs = useMemo(() => new DataSet(KeyOptionsDataSet(projectId, envId, appId)), [projectId, envId, appId]);

  const portDs = useMemo(() => new DataSet(PortDataSet({ formatMessage, projectId, envId, appId })), [projectId, envId, appId]);
  const targetLabelsDs = useMemo(() => new DataSet(TargetLabelsDataSet({ formatMessage, keyOptionsDs })), [keyOptionsDs]);
  
  const formDs = useMemo(() => new DataSet(CreateFormDataSet({ formatMessage, portDs, targetLabelsDs, appInstanceOptionsDs, networkStore, projectId, envId, appId })), [projectId, envId, appId]);

  const value = {
    ...props,
    formDs,
    portDs,
    targetLabelsDs,
    appInstanceOptionsDs,
    keyOptionsDs,
  };

  return (
    <NetWorkStore.Provider value={value}>
      {children}
    </NetWorkStore.Provider>
  );
}


export const NetWorkStoreProvider = injectIntl(inject('AppState')(StoreProvider));
export default useNetWorkStore;
