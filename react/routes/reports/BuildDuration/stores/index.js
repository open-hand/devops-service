import React, { createContext, useContext } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import { DataSet } from 'choerodon-ui/pro';
import buildDurationSelectDataSet from './BuildDurationSelectDataSet';

const Store = createContext();

export function useBuildDurationStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    intl: { formatMessage },
    children,
  } = props;

  const BuildDurationSelectDataSet = new DataSet(buildDurationSelectDataSet({ formatMessage }));

  const value = {
    ...props,
    BuildDurationSelectDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
