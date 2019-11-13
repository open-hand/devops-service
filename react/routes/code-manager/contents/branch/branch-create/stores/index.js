import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import CreateDataSet from './BranchCreateDataSet';
import issueNameDataSet from './IssueNameDataSet';
import useStore from './useStore';


const Store = createContext();

export function useFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
      appServiceId,
      intlPrefix,
    } = props;

    const contentStore = useStore();
    const issueNameOptionDs = useMemo(() => new DataSet(issueNameDataSet({ projectId }), [projectId]));
    const formDs = useMemo(() => new DataSet(CreateDataSet({ formatMessage, issueNameOptionDs, projectId, appServiceId, contentStore }), [projectId]));

    useEffect(() => {
      if (appServiceId) {
        formDs.transport = {
          create: ({ data: [data] }) => ({
            url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/branch`,
            method: 'post',
            transformRequest: () => {
              const issueId = data.issueName;
              const originBranch = data.branchOrigin;
              const type = data.branchType;
              const branchName = type === 'custom' ? data.branchName : `${data.branchType}-${data.branchName}`;
              
              const postData = {
                branchName,
                issueId,
                originBranch: originBranch && originBranch.slice(0, -7),
                type,
              };
              return JSON.stringify(postData);
            },
          }),
        };
      }
    }, [projectId, appServiceId]);

    const value = {
      ...props,
      projectId,
      appServiceId,
      contentStore,
      issueNameOptionDs,
      formDs,
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
