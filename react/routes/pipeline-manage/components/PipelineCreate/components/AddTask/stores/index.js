import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import addTaskFormDataSet from './addTaskFormDataSet';
import addTaskStepFormDataSet from './addTaskStepFormDataSet';
import appServiceOptionsDs from '../../../stores/appServiceOptionsDs';
import dependRepoDataSet from './dependRepoDataSet';
import useStore from './useStore';
import ZpkOptionsDataSet from './ZpkOptionsDataSet';

const Store = createContext();

export function useAddTaskStore() {
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

  const AddTaskUseStore = useStore();

  const AppServiceOptionsDs = useMemo(() => new DataSet(appServiceOptionsDs(projectId)), []);
  const ZpkOptionsDs = useMemo(() => new DataSet(ZpkOptionsDataSet({ organizationId, projectId })), [organizationId, projectId]);

  const AddTaskFormDataSet = useMemo(() => new DataSet(addTaskFormDataSet(props.PipelineCreateFormDataSet || '', AppServiceOptionsDs, props.appServiceId || '', projectId, AddTaskUseStore, organizationId, ZpkOptionsDs)), []);
  const AddTaskStepFormDataSet = useMemo(() => new DataSet(addTaskStepFormDataSet()), []);
  const DependRepoDataSet = useMemo(() => new DataSet(dependRepoDataSet()), []);

  const value = {
    ...props,
    AddTaskFormDataSet,
    AddTaskStepFormDataSet,
    DependRepoDataSet,
    AddTaskUseStore,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
