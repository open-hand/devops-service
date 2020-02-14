import React, { createContext, useContext } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import { DataSet } from 'choerodon-ui/pro';
import submissionSelectDataSet from './SubmissionSelectDataSet';

const Store = createContext();

export function useSubmissionStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    intl: { formatMessage },
    children,
  } = props;

  const SubmissionSelectDataSet = new DataSet(submissionSelectDataSet({ formatMessage }));

  const value = {
    ...props,
    SubmissionSelectDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
