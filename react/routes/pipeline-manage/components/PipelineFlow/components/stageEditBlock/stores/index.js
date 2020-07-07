import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from '../../../../../stores/useStore';
import useStepStore from './useStore';
import AddStepFormDataSet from './AddStepDataset';
import useEditBlockStore from '../../../../../stores/useEditBlockStore';

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
  } = props;
  const addStepDs = useMemo(() => new DataSet(AddStepFormDataSet(projectId)), []);

  const value = {
    ...props,
    stepStore,
    addStepDs,
    pipelineId,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
