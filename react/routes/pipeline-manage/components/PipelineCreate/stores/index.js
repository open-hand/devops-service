import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import pipelineCreateFormDataSet from './pipelineCreateFormDataSet';
import appServiceOptionsDs from './appServiceOptionsDs';

const Store = createContext();

export function usePipelineCreateStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
    AppState: {
      menuType: {
        projectId,
      },
    },
  } = props;

  const AppServiceOptionsDs = useMemo(() => new DataSet(appServiceOptionsDs(projectId)), []);
  const PipelineCreateFormDataSet = useMemo(() => new DataSet(pipelineCreateFormDataSet(AppServiceOptionsDs)), []);

  const value = {
    ...props,
    PipelineCreateFormDataSet,
    AppServiceOptionsDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
