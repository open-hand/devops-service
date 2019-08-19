import React, { createContext, useContext } from 'react';
import useStore from './useStore';

const Store = createContext();

export function useModalStore() {
  return useContext(Store);
}

export const StoreProvider = (props) => {
  const { children } = props;
  const modalStore = useStore();

  const value = {
    ...props,
    permissions: [
      'devops-service.application-instance.pageByOptions',
    ],
    modalStore,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
};
