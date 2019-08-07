import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import CasesDataSet from './CasesDataSet';
import { useDeploymentStore } from '../../../../stores';
import useStore from './useStore';

const Store = createContext();

export function useInstanceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const { deploymentStore: { getSelectedMenu: { menuId } } } = useDeploymentStore();
    const casesDs = useMemo(() => new DataSet(CasesDataSet(id, menuId)), [id, menuId]);
    const istStore = useStore();

    const value = {
      ...props,
      tabs: {
        CASES_TAB: 'cases',
        DETAILS_TAB: 'details',
        PODS_TAB: 'pods',
      },
      casesDs,
      istStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
