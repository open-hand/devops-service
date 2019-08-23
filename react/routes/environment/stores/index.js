import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import TreeDataSet from './TreeDataSet';
import useStore from './useStore';

const Store = createContext();

export function useEnvironmentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { id } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.env';
    const envStore = useStore();
    const treeDs = useMemo(() => new DataSet(TreeDataSet(id, envStore, formatMessage, intlPrefix)), [id]);

    const value = {
      ...props,
      intlPrefix,
      prefixCls: 'c7ncd-env',
      permissions: [],
      itemType: {
        DETAIL_ITEM: 'detail',
        GROUP_ITEM: 'group',
      },
      envStore,
      treeDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
