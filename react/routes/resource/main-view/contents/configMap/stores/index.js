import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import ConfigMapTableDataSet from './ConfigMapTableDataSet';
import { useResourceStore } from '../../../../stores';
import useConfigMapStore from '../../../stores/useConfigMapStore';
import getTablePostData from '../../../../../../utils/getTablePostData';

const Store = createContext();

export function useKeyValueStore() {
  return useContext(Store);
}


export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const {
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { parentId }, setUpTarget, getUpTarget },
      itemTypes: { MAP_GROUP },
    } = useResourceStore();
    

    const ConfigMapTableDs = useMemo(() => new DataSet(ConfigMapTableDataSet({ formatMessage })), []);
    const formStore = useConfigMapStore();

    useEffect(() => {
      ConfigMapTableDs.transport = {
        read: ({ data }) => {
          const postData = getTablePostData(data);
          return ({
            url: `/devops/v1/projects/${id}/config_maps/page_by_options?env_id=${parentId}`,
            method: 'post',
            data: postData,
          });
        },
        destroy: ({ data: [data] }) => ({
          url: `/devops/v1/projects/${id}/config_maps/${data.id}`,
          method: 'delete',
          data,
        }),
      };
      ConfigMapTableDs.query();
    }, [id, parentId]);

    const value = {
      ...props,
      permissions: {
        edit: ['devops-service.devops-config-map.update'],
        delete: ['devops-service.devops-config-map.delete'],
      },
      formStore,
      ConfigMapTableDs,
    };

    useEffect(() => {
      const { type, id: envId } = getUpTarget;
      if (parentId === envId) {
        if (type === MAP_GROUP) {
          ConfigMapTableDs.query();
          setUpTarget({});
        }
      }
    }, [getUpTarget]);

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
