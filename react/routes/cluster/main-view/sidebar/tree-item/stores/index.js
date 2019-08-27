import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';

const Store = createContext();

export function useTreeStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children, AppState: { currentMenuType: { projectId } } } = props;
    const treeItemStore = useStore();

    const value = {
      ...props,
      projectId,
      treeItemStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
