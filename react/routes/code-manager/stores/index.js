import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';
import DevPipelineStore from './DevPipelineStore';

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
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
