import React, { createContext, useContext } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './ReportsStore';
import submissionSelectDataSet from './SubmissionSelectDataSet';
import buildNumberSelectDataSet from './BuildNumberSelectDataSet';

const Store = createContext();

export function useReportsStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    AppState,
    children,
    intl: { formatMessage },
  } = props;

  const ReportsStore = useStore(AppState);
  const SubmissionSelectDataSet = new DataSet(submissionSelectDataSet({ formatMessage }));
  const BuildNumberSelectDataSet = new DataSet(buildNumberSelectDataSet({ formatMessage }));

  const value = {
    ...props,
    ReportsStore,
    SubmissionSelectDataSet,
    BuildNumberSelectDataSet,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));

export default StoreProvider;
