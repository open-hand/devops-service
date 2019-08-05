import React, { createContext, useMemo, useContext } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import TableDataSet from './TableDataSet';
import { useDeploymentStore } from '../../../../../stores';

const Store = createContext();

export function usePodsDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, intl, children } = props;
    const {
      deploymentStore: { getSelectedMenu: { menuId, parentId } },
      intlPrefix,
    } = useDeploymentStore();
    
    const tableDs = useMemo(() => {
      const [envId, appId] = parentId.split('-');
      return new DataSet(TableDataSet({
        intl,
        intlPrefix,
        projectId: id,
        envId,
        appId,
        istId: menuId,
      }));
    }, [id, intl, intlPrefix, menuId, parentId]);
    
    const value = {
      ...props,
      tableDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
