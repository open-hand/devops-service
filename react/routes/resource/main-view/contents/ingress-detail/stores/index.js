import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { useDeploymentStore } from '../../../../stores';
import DetailDataSet from './DetailDataSet';

const Store = createContext();

export function useCustomDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const {
      intlPrefix,
      intl: { formatMessage },
      deploymentStore: { getSelectedMenu: { menuId } },
    } = useDeploymentStore();
    const detailDs = useMemo(() => new DataSet(DetailDataSet({
      projectId: id,
      id: menuId,
    })), [id, menuId]);
  
    const value = {
      ...props,
      detailDs,
    };
  
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
