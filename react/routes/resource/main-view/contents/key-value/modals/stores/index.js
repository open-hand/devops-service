import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { useKeyValueStore } from '../../stores';

const Store = createContext();

export function useModalStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;
    const {
      itemType,
    } = useKeyValueStore();
    const permissions = {
      configMap: ['devops-service.devops-config-map.create'],
      secret: ['devops-service.devops-secret.createOrUpdate'],
    };

    const value = {
      ...props,
      permissions: permissions[itemType],
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
