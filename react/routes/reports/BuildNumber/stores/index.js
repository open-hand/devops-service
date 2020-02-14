import React, { createContext, useContext } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import { DataSet } from 'choerodon-ui/pro';
import buildNumberSelectDataSet from './BuildNumberSelectDataSet';

const Store = createContext();

export function useBuildNumberStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    intl: { formatMessage },
    children,
  } = props;

  const BuildNumberSelectDataSet = new DataSet(buildNumberSelectDataSet({ formatMessage }));

  const value = {
    ...props,
    BuildNumberSelectDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
