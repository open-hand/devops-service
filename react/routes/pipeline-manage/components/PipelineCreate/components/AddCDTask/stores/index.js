import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import addCDTaskDataSet from './addCDTaskDataSet';
import useStore from './useStore';

const Store = createContext();

export function useAddCDTaskStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
    // PipelineCreateFormDataSet,
    // AppServiceOptionsDs,
    PipelineCreateFormDataSet,
    AppState: {
      menuType: {
        projectId,
        organizationId,
      },
    },
  } = props;

  const ADDCDTaskUseStore = useStore();
  const ADDCDTaskDataSet = useMemo(() => new DataSet(addCDTaskDataSet(projectId, PipelineCreateFormDataSet, organizationId, ADDCDTaskUseStore)), [ADDCDTaskUseStore]);

  const value = {
    ...props,
    ADDCDTaskUseStore,
    ADDCDTaskDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
