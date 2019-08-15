import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import BaseInfoDataSet from './BaseInfoDataSet';
import ResourceCountDataSet from './ResourceCountDataSet';
import { useResourceStore } from '../../../../stores';

const Store = createContext();

export function useEnvironmentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { intl, AppState: { currentMenuType: { id } }, children } = props;
    const { intlPrefix, resourceStore: { getSelectedMenu: { menuId } } } = useResourceStore();
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
    const resourceCountDs = useMemo(() => new DataSet(ResourceCountDataSet(id, menuId)), [id, menuId]);
    // TODO: 设置 autoQuery 为 false，在切换tab时手动 query

    const value = {
      ...props,
      tabs: {
        SYNC_TAB: 'sync',
        ASSIGN_TAB: 'assign',
      },
      baseInfoDs,
      resourceCountDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
