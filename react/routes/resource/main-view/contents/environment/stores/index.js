import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import BaseInfoDataSet from './BaseInfoDataSet';
import PermissionsDataSet from './PermissionsDataSet';
import { useResourceStore } from '../../../../stores';
import useStore from './useStore';

const Store = createContext();

export function useEnvironmentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { intl, AppState: { currentMenuType: { id } }, children } = props;
    const { intlPrefix, resourceStore: { getSelectedMenu: { menuId } } } = useResourceStore();
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);

    useEffect(() => {
      baseInfoDs.transport.read.url = `/devops/v1/projects/${id}/envs/${menuId}/info`;
      baseInfoDs.query();
    }, [id, menuId]);

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
  })
));
