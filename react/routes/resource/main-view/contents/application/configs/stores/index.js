import React, { createContext, useContext } from 'react';
import { observer } from 'mobx-react-lite';
import { useApplicationStore } from '../../stores';

const Store = createContext();

export default Store;

export function useConfigsStore() {
  return useContext(Store);
}

export const StoreProvider = observer((props) => {
  const { children } = props;
  const {
    tabs: {
      MAPPING_TAB,
    },
    appStore: { getTabKey },
    mappingStore,
    cipherStore,
    mappingDs,
    cipherDs,
  } = useApplicationStore();

  const formStore = getTabKey === MAPPING_TAB ? mappingStore : cipherStore;
  const tableDs = getTabKey === MAPPING_TAB ? mappingDs : cipherDs;
  const value = {
    ...props,
    tableDs,
    formStore,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
});
