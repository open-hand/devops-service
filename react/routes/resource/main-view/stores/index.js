import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';
import useNetworkStore from './useNetworkStore';
import useCustomStore from './useCustomStore';
import useIngressStore from './useIngressStore';
import useConfigMapStore from './useConfigMapStore';
import useSecretStore from './useSecretStore';

const Store = createContext();

export function useMainStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;
    const mainStore = useStore();

    const networkStore = useNetworkStore();
    const customStore = useCustomStore();
    const ingressStore = useIngressStore();
    const configMapStore = useConfigMapStore();
    const secretStore = useSecretStore();

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      podColor: {
        RUNNING_COLOR: '#0bc2a8',
        PADDING_COLOR: '#fbb100',
      },
      mainStore,
      networkStore,
      customStore,
      ingressStore,
      configMapStore,
      secretStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
