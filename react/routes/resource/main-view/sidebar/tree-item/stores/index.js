import React, { createContext, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from './useStore';
import { useResourceStore } from '../../../../stores';

const Store = createContext();

export function useTreeItemStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children, AppState: { currentMenuType: { id } } } = props;
    const {
      resourceStore: {
        getSelectedMenu: {
          itemType,
          parentId,
        },
      },
      itemType: {
        SERVICES_ITEM,
        INGRESS_ITEM,
        CERT_ITEM,
        MAP_ITEM,
        CIPHER_ITEM,
        CUSTOM_ITEM,
      },
      treeDs,
    } = useResourceStore();
    const treeItemStore = useStore();

    useEffect(() => {
      if (parentId) {
        const envId = parentId.split('-')[0];
        treeDs.transport.destroy = ({ data: [data] }) => {
          const url = {
            [SERVICES_ITEM]: `/devops/v1/projects/${id}/service/${data.id}`,
            [INGRESS_ITEM]: `/devops/v1/projects/${id}/ingress/${data.id}`,
            [CERT_ITEM]: `/devops/v1/projects/${id}/certifications?cert_id=${data.id}`,
            [MAP_ITEM]: `/devops/v1/projects/${id}/config_maps/${data.id}`,
            [CIPHER_ITEM]: `/devops/v1/projects/${id}/secret/${envId}/${data.id}`,
            [CUSTOM_ITEM]: `/devops/v1/projects/${id}/customize_resource?resource_id=${data.id}`,
          };
          return ({
            url: url[itemType],
            method: 'delete',
          });
        };
      }
    }, [id, itemType, parentId]);

    const value = {
      ...props,
      treeItemStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
