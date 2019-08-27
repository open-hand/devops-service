import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useEnvironmentStore } from '../../stores';
import EnvFormDataSet from './EnvFormDataSet';
import GroupFormDataSet from './GroupFormDataSet';
import useStore from './useStore';

const Store = createContext();

export function useMainStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { intlPrefix } = useEnvironmentStore();
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const mainStore = useStore();
    const envFormDs = useMemo(() => new DataSet(EnvFormDataSet({ formatMessage, intlPrefix, projectId })), []);
    const groupFormDs = useMemo(() => new DataSet(GroupFormDataSet({ formatMessage, intlPrefix, projectId })), [projectId]);

    const value = {
      ...props,
      mainStore,
      envFormDs,
      groupFormDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
