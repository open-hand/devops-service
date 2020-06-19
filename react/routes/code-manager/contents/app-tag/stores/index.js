import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import HandleMapStore from '../../../main-view/store/handleMapStore';
import AppTagDataSet from './AppTagDataSet';
import AppTagCreateDataSet from './AppTagCreateDataSet';

import { useCodeManagerStore } from '../../../stores';
import useStore from './useStore';

const AppTagStore = createContext();

export function useAppTagStore() {
  return useContext(AppTagStore);
}

export const AppTagStoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const { children, intl: { formatMessage }, AppState: { currentMenuType: { projectId } } } = props;
  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  const appServiceId = selectAppDs.current.get('appServiceId');
  const tagStore = useStore();
  const [appTagDs, appTagCreateDs] = useMemo(() => [new DataSet(AppTagDataSet(projectId, appServiceId, tagStore)), new DataSet(AppTagCreateDataSet(formatMessage, projectId, appServiceId, tagStore))], [projectId, appServiceId]);
  
  useEffect(() => {
    if (!appServiceId) return;
    appTagDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_tags_by_options`;
    appTagDs.query();
  }, [appServiceId]);
  const value = {
    formatMessage,
    handleMapStore: HandleMapStore,
    appTagDs,
    appTagCreateDs,
    tagStore,
    projectId,
  };

  return (
    <AppTagStore.Provider value={value}>
      {children}
    </AppTagStore.Provider>
  );
})));
