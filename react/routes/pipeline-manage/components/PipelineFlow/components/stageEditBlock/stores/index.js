import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import AddStepFormDataSet from './AddStepDataset';

const Store = createContext();

export function usePipelineStageEditStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
    pipelineId,
    stepStore,
    AppState: { currentMenuType: { projectId } },
    appServiceId,
  } = props;
  
  const addStepDs = useMemo(() => new DataSet(AddStepFormDataSet(projectId)), [projectId, appServiceId]);

  const value = {
    ...props,
    stepStore,
    addStepDs,
    pipelineId,
    projectId,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
