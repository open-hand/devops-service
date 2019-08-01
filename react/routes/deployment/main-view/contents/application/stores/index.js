import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import BaseInfoDataSet from './BaseInfoDataSet';
import { useDeploymentStore } from '../../../../stores';

const Store = createContext();

export function useApplicationStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      deploymentStore: { getSelectedMenu: { menuId } },
    } = useDeploymentStore();
    const { AppState: { currentMenuType: { id } }, children } = props;
    
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);

    const value = {
      ...props,
      baseInfoDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
