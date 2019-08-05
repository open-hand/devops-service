import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import BaseInfoDataSet from './BaseInfoDataSet';
import PermissionsDataSet from './PermissionsDataSet';
import { useDeploymentStore } from '../../../../stores';
import useStore from './useStore';

const Store = createContext();

export function useEnvironmentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { intl, AppState: { currentMenuType: { id } }, children } = props;
    const { intlPrefix, deploymentStore: { getSelectedMenu: { menuId } } } = useDeploymentStore();
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
    // TODO: 设置 autoQuery 为 false，在切换tab时手动 query
    const permissionsDs = useMemo(() => new DataSet(PermissionsDataSet({
      intl,
      intlPrefix,
      projectId: id,
      envId: menuId,
    })), [id, intl, intlPrefix, menuId]);
    const envStore = useStore();

    const value = {
      ...props,
      tabs: {
        SYNC_TAB: 'sync',
        ASSIGN_TAB: 'assign',
      },
      baseInfoDs,
      permissionsDs,
      envStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
