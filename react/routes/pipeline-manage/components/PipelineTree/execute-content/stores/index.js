import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import SelectDataSet from './SelectDataSet';

const Store = createContext();

export function useExecuteContentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    children,
  } = props;

  const selectDs = useMemo(() => new DataSet(SelectDataSet({ projectId, formatMessage })), [projectId]);

  const value = {
    ...props,
    selectDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
