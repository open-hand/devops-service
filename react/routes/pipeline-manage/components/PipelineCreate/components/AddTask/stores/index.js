import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import addTaskFormDataSet from './addTaskFormDataSet';
import addTaskStepFormDataSet from './addTaskStepFormDataSet';
import useStore from './useStore';

const Store = createContext();

export function useAddTaskStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
    PipelineCreateFormDataSet,
    AppServiceOptionsDs,
  } = props;


  const AddTaskFormDataSet = useMemo(() => new DataSet(addTaskFormDataSet(PipelineCreateFormDataSet, AppServiceOptionsDs)), []);
  const AddTaskStepFormDataSet = useMemo(() => new DataSet(addTaskStepFormDataSet()), []);

  const value = {
    ...props,
    AddTaskFormDataSet,
    AddTaskStepFormDataSet,
    useStore: useStore(),
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
