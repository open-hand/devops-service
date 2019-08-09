import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';

const Store = createContext();

export function useModalStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;

    const value = {
      ...props,
      permissions: [
        'devops-service.devops-ingress.create',
      ],
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
