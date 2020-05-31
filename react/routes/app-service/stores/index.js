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
    const listPermissions = useMemo(() => (['choerodon.code.project.develop.app-service.ps.default']), []);
    const detailPermissions = useMemo(() => ([]), []);

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
