import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import HandleMapStore from '../../../store/handleMapStore';

import { useCodeManagerStore } from '../../../../stores';
import useStore from './useStore';


const CodeQualityStore = createContext();

export function useCodeQualityStore() {
  return useContext(CodeQualityStore);
}

export const CodeQualityStoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const { children, intl: { formatMessage }, AppState: { currentMenuType: { projectId } } } = props;
  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  const appServiceId = selectAppDs.current.get('appServiceId');
  const codeQualityStore = useStore();
  
  useEffect(() => {
    if (!appServiceId) return;
    codeQualityStore.loadCodeQualityData(projectId, appServiceId);
  }, [appServiceId]);
  const value = {
    formatMessage,
    handleMapStore: HandleMapStore,
    codeQuality: codeQualityStore,
    projectId,
  };

  return (
    <CodeQualityStore.Provider value={value}>
      {children}
    </CodeQualityStore.Provider>
  );
})));
