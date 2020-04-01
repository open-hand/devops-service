import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import addTaskFormDataSet from './addTaskFormDataSet';

const Store = createContext();

export function useAddTaskStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
  } = props;

  const AddTaskFormDataSet = useMemo(() => new DataSet(addTaskFormDataSet()), []);

  const value = {
    ...props,
    AddTaskFormDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
