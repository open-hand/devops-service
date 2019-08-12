import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useDeploymentStore } from '../../../../stores';
import CasesDataSet from './CasesDataSet';
import PodsDataset from './PodsDataSet';
import DetailsStore from './DetailsStore';
import useStore from './useStore';

const Store = createContext();

export default Store;

export function useInstanceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children, intl } = props;
    const {
      deploymentStore: {
        getSelectedMenu: {
          menuId,
          parentId,
        } },
      intlPrefix,
    } = useDeploymentStore();
    const [envId, appId] = parentId.split('-');

    const detailsStore = useMemo(() => new DetailsStore(), []);
    const casesDs = useMemo(() => new DataSet(CasesDataSet(id, menuId)), [id, menuId]);
    const podsDs = useMemo(() => new DataSet(PodsDataset({
      intl,
      intlPrefix,
      projectId: id,
      envId,
      appId,
      istId: menuId,
    })), [appId, envId, id, intl, intlPrefix, menuId]);
    const istStore = useStore();

    const value = {
      ...props,
      tabs: {
        CASES_TAB: 'cases',
        DETAILS_TAB: 'details',
        PODS_TAB: 'pods',
      },
      casesDs,
      podsDs,
      istStore,
      detailsStore,
      instanceId: menuId,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
