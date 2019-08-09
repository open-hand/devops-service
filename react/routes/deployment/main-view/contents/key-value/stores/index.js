import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import TableDataSet from './TableDataSet';
import { useDeploymentStore } from '../../../../stores';

const TYPE = {
  group_configMaps: 'configMap',
  group_secrets: 'secret',
};

const Store = createContext();

export function useCertificateStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children, contentType } = props;
    const {
      intlPrefix,
      intl: { formatMessage },
      deploymentStore: { getSelectedMenu: { parentId } },
    } = useDeploymentStore();
    const itemType = TYPE[contentType];
    
    const listDs = useMemo(() => new DataSet(TableDataSet({
      formatMessage,
      intlPrefix,
      itemType,
      projectId: id,
      envId: parentId,
    })), [formatMessage, id, intlPrefix, itemType, parentId]);
  
    const value = {
      ...props,
      itemType,
      listDs,
    };
  
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
