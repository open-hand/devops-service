import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';

const Store = createContext();

export function useCodeManagerStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;
    const codeManagerStore = useStore();

    const value = {
      ...props,
      prefixCls: 'c7ncd-code-manager',
      intlPrefix: 'c7ncd.code-manager',
      permissions: [
        'devops-service.application-instance.pageByOptions',
      ],
      viewType: {
        IST_VIEW_TYPE: 'instance',
        RES_VIEW_TYPE: 'resource',
      },
      itemType: {
        ENV_ITEM: 'environment',
        APP_ITEM: 'application',
        IST_ITEM: 'instances',
        GROUP_ITEM: 'group',
        SERVICES_ITEM: 'services',
        INGRESS_ITEM: 'ingresses',
        CERT_ITEM: 'certifications',
        MAP_ITEM: 'configMaps',
        CIPHER_ITEM: 'secrets',
        CUSTOM_ITEM: 'customResources',
      },
      codeManagerStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
