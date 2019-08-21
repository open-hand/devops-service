import React, { useContext, createContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useClusterStore } from '../../stores';

const Store = createContext();

export function useClusterMainStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const { children } = props;
  const value = {
    ...props,
    prefixCls: 'c7ncd-cluster',
    intlPrefix: 'c7ncd.cluster',
    permissions: [],
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
})));
