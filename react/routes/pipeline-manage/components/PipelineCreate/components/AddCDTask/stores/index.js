import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import addCDTaskDataSet from './addCDTaskDataSet';

const Store = createContext();

export function useAddCDTaskStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
    // PipelineCreateFormDataSet,
    // AppServiceOptionsDs,
    AppState: {
      menuType: {
        projectId,
        organizationId,
      },
    },
  } = props;

  const ADDCDTaskDataSet = useMemo(() => new DataSet(addCDTaskDataSet(projectId)), []);

  const value = {
    ...props,
    ADDCDTaskDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
