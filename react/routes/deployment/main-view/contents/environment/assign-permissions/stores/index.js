import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import TableDataSet from './TableDataSet';
import { useDeploymentStore } from '../../../../../stores';

const Store = createContext();

export function useAssignStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children, intl } = props;
    const {
      prefixCls,
      intlPrefix,
      deploymentStore: { getSelectedMenu: { menuId } },
    } = useDeploymentStore();
    const tableDs = useMemo(() => new DataSet(TableDataSet({
      intl,
      intlPrefix,
      projectId: id,
      envId: menuId,
    })), [id, intl, intlPrefix, menuId]);

    const value = {
      ...props,
      tableDs,
      prefixCls,
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
