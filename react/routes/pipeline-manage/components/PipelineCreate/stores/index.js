import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import pipelineCreateFormDataSet from './pipelineCreateFormDataSet';

const Store = createContext();

export function usePipelineCreateStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
  } = props;

  const PipelineCreateFormDataSet = useMemo(() => new DataSet(pipelineCreateFormDataSet()), []);

  const value = {
    ...props,
    PipelineCreateFormDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
