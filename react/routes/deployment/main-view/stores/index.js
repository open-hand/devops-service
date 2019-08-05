import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';

const Store = createContext();

export function useMainStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;
    const mainStore = useStore();

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      podColor: {
        RUNNING_COLOR: '#0bc2a8',
        PADDING_COLOR: '#fbb100',
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
      mainStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
