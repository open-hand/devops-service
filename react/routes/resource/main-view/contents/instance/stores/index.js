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
import openWarnModal from '../../../../../../utils/openWarnModal';
import getTablePostData from '../../../../../../utils/getTablePostData';

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
        getUpTarget,
      },
      resourceStore,
      intlPrefix,
      treeDs,
    } = useResourceStore();
    const istStore = useStore({ defaultKey: getViewType === 'instance' ? 'cases' : 'details' });

    const tabs = useMemo(() => ({
      CASES_TAB: 'cases',
      DETAILS_TAB: 'details',
      PODS_TAB: 'pods',
    }), []);
    const detailsStore = useMemo(() => new DetailsStore(), []);
    const baseDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
    const casesDs = useMemo(() => new DataSet(CasesDataSet()), []);
    const podsDs = useMemo(() => new DataSet(PodsDataset({
      intl,
      intlPrefix,
    })), []);

    function freshTree() {
      treeDs.query();
    }

    function queryData() {
      casesDs.reset();
      const tabKey = istStore.getTabKey;
      switch (tabKey) {
        case tabs.CASES_TAB:
          casesDs.query();
          break;
        case tabs.DETAILS_TAB:
          detailsStore.loadResource(projectId, id);
          break;
        case tabs.PODS_TAB:
          podsDs.query();
          break;
        default:
      }
    }

    function checkIstExist() {
      const [envId] = parentId.split('**');
      return resourceStore.checkExist({
        projectId,
        type: 'instance',
        envId,
        id,
      }).then((isExist) => {
        if (!isExist) {
          openWarnModal(freshTree, intl.formatMessage);
        }
        return isExist;
      });
    }

    useEffect(() => {
      checkIstExist().then((query) => {
        if (query) {
          baseDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_instances/${id}`;
          baseDs.query();
          casesDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_instances/${id}/events`;
          podsDs.transport.read = ({ data }) => {
            const [envId, appId] = parentId.split('**');
            const postData = getTablePostData(data);
            const param = getViewType === 'instance' ? `&app_service_id=${appId}` : '';
            return {
              url: `devops/v1/projects/${projectId}/pods/page_by_options?env_id=${envId}&instance_id=${id}${param}`,
              method: 'post',
              data: postData,
            };
          };
          podsDs.transport.destroy = ({ data }) => {
            const [envId, appId] = parentId.split('**');
            const podId = data[0].id;
            return {
              url: `devops/v1/projects/${projectId}/pods/${podId}?env_id=${envId}`,
              method: 'delete' };
          };
          queryData();
        }
      });
    }, [projectId, id, istStore.getTabKey, parentId]);

    useEffect(() => {
      const { type, id: istId } = getUpTarget;
      if (type === 'instances' && istId === id) {
        queryData();
        resourceStore.setUpTarget({});
      }
    }, [getUpTarget]);

    const value = {
      ...props,
      tabs,
      baseDs,
      casesDs,
      podsDs,
      istStore,
      detailsStore,
      intlPrefix,
      checkIstExist,
      instanceId: id,
      envId: parentId.split('**')[0],
      treeDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }),
));
