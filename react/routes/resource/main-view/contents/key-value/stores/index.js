import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import TableDataSet from './TableDataSet';
import { useResourceStore } from '../../../../stores';
import useConfigMapStore from '../../../stores/useConfigMapStore';
import useSecretStore from '../../../stores/useSecretStore';

const TYPE = {
  group_configMaps: 'configMap',
  group_secrets: 'secret',
};

const Store = createContext();

export function useKeyValueStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children, contentType } = props;
    const {
      intlPrefix,
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { parentId } },
    } = useResourceStore();
    const itemType = TYPE[contentType];

    const listDs = useMemo(() => new DataSet(TableDataSet({
      formatMessage,
      itemType,
      projectId: id,
      envId: parentId,
    })), [formatMessage, id, itemType, parentId]);

    const itemData = {
      configMap: {
        permissions: {
          edit: ['devops-service.devops-config-map.create'],
          delete: ['devops-service.devops-config-map.delete'],
        },
        formStore: useConfigMapStore(),
      },
      secret: {
        permissions: {
          edit: ['devops-service.devops-secret.createOrUpdate'],
          delete: ['devops-service.devops-secret.deleteSecret'],
        },
        formStore: useSecretStore(),
      },
    };

    const value = {
      ...props,
      itemType,
      listDs,
      permissions: itemData[itemType].permissions,
      formStore: itemData[itemType].formStore,
    };

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
