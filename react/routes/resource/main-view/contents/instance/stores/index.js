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
    const { AppState: { currentMenuType: { id: projectId } }, children, intl } = props;
    const {
      resourceStore: {
        getSelectedMenu: { id, parentId },
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
        projectId,
        envId,
        appId: getViewType === 'instance' ? appId : '',
        id,
      }));
    }, [projectId, parentId, id, getViewType]);

    const tabKey = istStore.getTabKey;

    useEffect(() => {
      baseDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_instances/${id}`;
      baseDs.query();
      casesDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_instances/${id}/events`;
      tabKey === tabs.CASES_TAB && casesDs.query();
      tabKey === tabs.DETAILS_TAB && detailsStore.loadResource(projectId, id);
      tabKey === tabs.PODS_TAB && podsDs.query();
    }, [projectId, id, tabKey]);

    const value = {
      ...props,
      tabs,
      baseDs,
      casesDs,
      podsDs,
      istStore,
      detailsStore,
      instanceId: id,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
