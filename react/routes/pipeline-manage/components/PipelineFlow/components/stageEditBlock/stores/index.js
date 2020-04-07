import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from '../../../../../stores/useStore';
import useStepStore from './useStore';
import AddStepFormDataSet from './AddStepDataset';

const Store = createContext();

export function usePipelineStageEditStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
  } = props;

  const stepStore = useStepStore();
  const addStepDs = useMemo(() => new DataSet(AddStepFormDataSet()), []);

  const value = {
    ...props,
    stepStore,
    addStepDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
