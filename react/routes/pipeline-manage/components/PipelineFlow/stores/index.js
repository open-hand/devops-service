import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { usePipelineManageStore } from '../../../stores';

const Store = createContext();

export function usePipelineFlowStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
    AppState: { currentMenuType: { projectId } },
    histroy,
    location,
  } = props;
  const {
    mainStore: {
      getSelectedMenu,
    },
  } = usePipelineManageStore();

  const value = {
    ...props,
    getSelectedMenu,
    projectId,
    histroy,
    location,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
