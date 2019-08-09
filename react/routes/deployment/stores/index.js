import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';

const Store = createContext();

export function useDeploymentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;
    const deploymentStore = useStore();

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
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
        SERVICES_ITEM: 'services',
        INGRESS_ITEM: 'ingresses',
        CERT_ITEM: 'certifications',
        MAP_ITEM: 'configMaps',
        CIPHER_ITEM: 'secrets',
        CUSTOM_ITEM: 'customResources',
        SERVICES_GROUP: 'group_services',
        INGRESS_GROUP: 'group_ingresses',
        CERT_GROUP: 'group_certifications',
        MAP_GROUP: 'group_configMaps',
        CIPHER_GROUP: 'group_secrets',

      },
      deploymentStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
