import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import SelectDataSet from './SelectDataSet';
import useStore from './useStore';

const Store = createContext();

export function useExecuteContentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    children,
    appServiceId,
    gitlabProjectId,
    pipelineId,
    appServiceName,
  } = props;

  const store = useStore();
  const selectDs = useMemo(() => new DataSet(SelectDataSet({ projectId, formatMessage, gitlabProjectId, pipelineId, appServiceName })), [projectId, gitlabProjectId, pipelineId]);

  useEffect(() => {
    store.loadBranchData({ projectId, appServiceId, page: 1 });
    store.loadTagData({ projectId, appServiceId, page: 1 });
  }, []);

  const value = {
    ...props,
    selectDs,
    store,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
