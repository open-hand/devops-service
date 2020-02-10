import React, { createContext, useContext, useMemo } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import { DataSet } from 'choerodon-ui/pro';
import formDataSet from './formDataSet';
import keyValueDataSet from './keyValueDataSet';

const Store = createContext();

export function useKeyValueStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    id,
    store,
    intl: { formatMessage },
    AppState: {
      currentMenuType: {
        projectId,
      },
    },
    envId,
    children,
    title,
  } = props;

  const KeyValueDataSet = useMemo(() => new DataSet(keyValueDataSet()), []);
  const FormDataSet = useMemo(() => new DataSet(formDataSet({ title, id, formatMessage, projectId, envId, store })), []);

  const value = {
    ...props,
    FormDataSet,
    KeyValueDataSet,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
