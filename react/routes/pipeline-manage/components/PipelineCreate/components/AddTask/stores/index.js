import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import addTaskFormDataSet from './addTaskFormDataSet';
import addTaskStepFormDataSet from './addTaskStepFormDataSet';

const Store = createContext();

export function useAddTaskStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
  } = props;

  const AddTaskFormDataSet = useMemo(() => new DataSet(addTaskFormDataSet()), []);
  const AddTaskStepFormDataSet = useMemo(() => new DataSet(addTaskStepFormDataSet()), []);

  const value = {
    ...props,
    AddTaskFormDataSet,
    AddTaskStepFormDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
