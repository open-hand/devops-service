import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import TableDataSet from './TableDataSet';
import { useDeploymentStore } from '../../../../../stores';

const Store = createContext();

export function useNetStore() {
  return useContext(Store);
}

export const StoreProvider = (props) => {
  const { children } = props;
  const {
    intlPrefix,
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
  } = useDeploymentStore();
  const tableDs = useMemo(() => new DataSet(TableDataSet({
    formatMessage,
    intlPrefix,
    projectId: id,
    // id: menuId,
  })), [formatMessage, id, intlPrefix]);

  const value = {
    tableDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
};
