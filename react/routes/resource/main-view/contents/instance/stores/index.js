import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useResourceStore } from '../../../../stores';
import BaseInfoDataSet from './BaseInfoDataSet';
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
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children, intl } = props;
    const {
      resourceStore: {
        getSelectedMenu: {
          menuId,
          parentId,
        },
        getViewType,
      },
      intlPrefix,
    } = useResourceStore();
    const istStore = useStore();

    const tabs = useMemo(() => ({
      CASES_TAB: 'cases',
      DETAILS_TAB: 'details',
      PODS_TAB: 'pods',
    }), []);
    const detailsStore = useMemo(() => new DetailsStore(), []);
    const baseDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
    const casesDs = useMemo(() => new DataSet(CasesDataSet()), []);
    const podsDs = useMemo(() => {
      const [envId, appId] = parentId.split('-');

      return new DataSet(PodsDataset({
        intl,
        intlPrefix,
        projectId: id,
        envId,
        appId: getViewType === 'instance' ? appId : '',
        id: menuId,
      }));
    }, [id, parentId, menuId, getViewType]);

    const tabKey = istStore.getTabKey;

    useEffect(() => {
      baseDs.transport.read.url = `/devops/v1/projects/${id}/app_service_instances/${menuId}`;
      baseDs.query();
      casesDs.transport.read.url = `/devops/v1/projects/${id}/app_service_instances/${menuId}/events`;
      tabKey === tabs.CASES_TAB && casesDs.query();
      tabKey === tabs.DETAILS_TAB && detailsStore.loadResource(id, menuId);
      tabKey === tabs.PODS_TAB && podsDs.query();
    }, [id, menuId, tabKey]);

    const value = {
      ...props,
      tabs,
      baseDs,
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
  })
));
