import React, { useContext, createContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useClusterStore } from '../../stores';
import useStore from './useStore';

const Store = createContext();

export function useClusterMainStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const { children } = props;
  const mainStore = useStore();
  const value = {
    ...props,
    prefixCls: 'c7ncd-cluster',
    intlPrefix: 'c7ncd.cluster',
    permissions: [],
    mainStore,
  };
  
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
})));
