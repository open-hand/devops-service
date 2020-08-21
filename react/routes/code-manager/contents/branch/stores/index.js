import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';

import { useCodeManagerStore } from '../../../stores';
import getTablePostData from '../../../../../utils/getTablePostData';
import TableDataset from './TableDataSet';
import useStore from './useStore';

const Store = createContext();

export function useTableStore() {
  return useContext(Store);
}
export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      appServiceDs,
      selectAppDs,
    } = useCodeManagerStore();

    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
    } = props;
    const branchStore = useStore();

    const appServiceId = selectAppDs.current && selectAppDs.current.get('appServiceId');
    const tableDs = useMemo(() => new DataSet(TableDataset({ projectId, formatMessage, appServiceId })), []);

    useEffect(() => {
      if (appServiceId) {
        tableDs.transport = {
          read: ({ data }) => ({
            url: `devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_branch_by_options`,
            method: 'post',
            data: JSON.stringify(getTablePostData(data)),
            transformResponse: (response) => {
              try {
                if (!response) {
                  branchStore.setIsEmpty(true);
                  return response;
                } else {
                  branchStore.setIsEmpty(false);
                }
                const result = JSON.parse(response);
                return result;
              } catch (e) {
                return response;
              }
            },
          }),
          destroy: ({ data: [data] }) => ({
            url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/branch?branch_name=${data.branchName}`,
            method: 'delete',
          }),
        };
        tableDs.query();
      }
    }, [projectId, appServiceId]);

    const value = {
      ...props,
      projectId,
      formatMessage,
      intlPrefix,
      tableDs,
      appServiceDs,
      appServiceId,
      branchStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
