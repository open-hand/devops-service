import React, { createContext, useContext } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import useStore from './ReportsStore';

const Store = createContext();

export function useReportsStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    AppState,
    children,
  } = props;
  const ReportsStore = useStore(AppState);

  const value = {
    ...props,
    ReportsStore,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));

export default StoreProvider;
