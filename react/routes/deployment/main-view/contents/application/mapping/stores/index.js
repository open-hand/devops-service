import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import TableDataSet from './TableDataSet';
import { useDeploymentStore } from '../../../../../stores';

const Store = createContext();

export function useMappingStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      deploymentStore: { getSelectedMenu: { parentId } },
      intlPrefix,
    } = useDeploymentStore();
    const {
      AppState: { currentMenuType: { id } },
      intl: { formatMessage },
      children,
    } = props;

    const tableDs = useMemo(() => {
      const [envId, appId] = parentId.split('-');
      return new DataSet(TableDataSet({
        formatMessage,
        intlPrefix,
        projectId: id,
        envId,
        appId,
      }));
    }, [formatMessage, id, intlPrefix, parentId]);
    const value = {
      ...props,
      tableDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
