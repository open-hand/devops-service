import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';

const Store = createContext();

export function useAppTopStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      children,
    } = props;
    const appServiceStore = useStore();
    const intlPrefix = 'c7ncd.appService';
    const listPermissions = useMemo(() => ([
      'devops-service.app-service.pageByOptions',
      'devops-service.app-service.create',
      'devops-service.app-service.importApp',
      'devops-service.app-service.update',
      'devops-service.app-service.updateActive',
      'devops-service.app-service.delete',
    ]), []);
    const detailPermissions = useMemo(() => ([
      'devops-service.app-service.query',
      'devops-service.app-service.update',
      'devops-service.app-service.updateActive',
      'devops-service.app-service-version.pageByOptions',
      'devops-service.app-share-rule.create',
      'devops-service.app-share-rule.update',
      'devops-service.app-share-rule.delete',
      'devops-service.app-share-rule.query',
      'devops-service.app-share-rule.pageByOptions',
      'devops-service.app-service.pagePermissionUsers',
      'devops-service.app-service.updatePermission',
      'devops-service.app-service.deletePermission',
      'devops-service.app-service.listNonPermissionUsers',
    ]), []);

    useEffect(() => {
      appServiceStore.checkHasApp(projectId);
    }, []);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      appServiceStore,
      listPermissions,
      detailPermissions,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
