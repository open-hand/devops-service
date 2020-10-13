import React, { createContext, useContext, useMemo } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';

const Store = createContext({} as any);

export function useHostConfigListsStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props: any) => {
  const {
    children,
    intl: { formatMessage },
    AppState: { currentMenuType: { projectId } },
  } = props;

  const value = {
    ...props,
    prefixCls: 'c7ncd-host-config',
    formatMessage,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
