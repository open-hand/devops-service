import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import SecretTableDataSet from './SecretTableDataSet';
import { useResourceStore } from '../../../../stores';
import useSecretStore from '../../../stores/useSecretStore';
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
      itemTypes: { CIPHER_GROUP },
    } = useResourceStore();

    const SecretTableDs = useMemo(() => new DataSet(SecretTableDataSet({ formatMessage })), []);
    const formStore = useSecretStore();

    useEffect(() => {
      SecretTableDs.transport = {
        read: ({ data }) => {
          const postData = getTablePostData(data);
          return ({
            url: `/devops/v1/projects/${id}/secret/page_by_options?env_id=${parentId}`,
            method: 'post',
            data: postData,
          });
        },
        destroy: ({ data: [data] }) => ({
          url: `/devops/v1/projects/${id}/secret/${parentId}/${data.id}`,
          method: 'delete',
          data,
        }),
      };
      SecretTableDs.query();
    }, [id, parentId]);

    const value = {
      ...props,
      permissions: {
        edit: ['devops-service.devops-secret.update'],
        delete: ['devops-service.devops-secret.deleteSecret'],
      },
      formStore,
      SecretTableDs,
    };

    useEffect(() => {
      const { type, id: envId } = getUpTarget;
      if (parentId === envId) {
        if (type === CIPHER_GROUP) {
          SecretTableDs.query();
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
