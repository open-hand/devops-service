import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import DevPipelineStore from './DevPipelineStore';
import AppServiceDs from './AppServiceDataSet';
import SelectAppDataSet from './SelectAppDataSet';
import handleMapStore from '../main-view/store/handleMapStore';

const Store = createContext();

export function useCodeManagerStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      children,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = props;
    const handleDataSetChange = ({ record, value, oldValue }) => {
      if (!value) return;
      
      const option = {
        props: {
          children: record.get('name'),
          value: record.get('id'),
        },
      };
      DevPipelineStore.setSelectApp(value);
      DevPipelineStore.setRecentApp(value);
      Object.keys(handleMapStore)
        .forEach((key) => {
          if (key.indexOf('Code') !== -1) {
            handleMapStore[key]
              && handleMapStore[key].select
              && handleMapStore[key].select(value, option);
          }
        });
    };
    const appServiceDs = useMemo(() => new DataSet(AppServiceDs({ projectId })), []);
    const selectAppDs = useMemo(() => new DataSet(SelectAppDataSet({ handleDataSetChange })), []);
    const codeManagerStore = useStore();
    const permissions = useMemo(() => ([
      'devops-service.devops-git.pageTagsByOptions',
      'devops-service.devops-git.createTag',
      'devops-service.devops-git.checkTag',
      'devops-service.devops-git.createBranch',
      'devops-service.devops-git.deleteBranch',
      'devops-service.devops-git.updateBranchIssue',
      'devops-service.devops-git.pageBranchByOptions',
      'devops-service.pipeline.pageByOptions',
      'devops-service.application.getSonarQube',
      'devops-service.devops-git.queryUrl',
      'devops-service.devops-git.listMergeRequest',
      'devops-service.app-service.listByActive',
      'choerodon.route.develop.code-management',
    ]), []);

    useEffect(() => {
      codeManagerStore.checkHasApp(projectId);
      DevPipelineStore.setAppData([]);
      DevPipelineStore.setRecentApp([]);
      DevPipelineStore.setSelectApp(null);
    }, []);
    useEffect(async () => {
      appServiceDs.transport.read = () => ({
        url: `/devops/v1/projects/${projectId}/app_service/list_by_active`,
        method: 'get',
      });
      const res = await appServiceDs.query();
      if (res && res.length && res.length > 0) {
        selectAppDs.current.set('appServiceId', res[0].id);
      }
    }, [projectId]);
    const value = {
      ...props,
      prefixCls: 'c7ncd-code-manager',
      intlPrefix: 'c7ncd.code-manager',
      itemType: {
        CIPHER_ITEM: 'secrets',
        CUSTOM_ITEM: 'customResources',
      },
      codeManagerStore,
      permissions,
      appServiceDs,
      selectAppDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
