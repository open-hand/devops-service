import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import useStore from './useStore';
import OptionsDataSet from './OptionsDataSet';
import { useResourceStore } from '../../../../../stores';
import useDeployStore from '../../../../../../deployment/stores/useStore';

const Store = createContext();

export function useModalStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const modalStore = useStore();
  const deployStore = useDeployStore();
  const {
    AppState: { currentMenuType: { projectId } },
    children,
    intl: { formatMessage },
  } = props;
  const {
    intlPrefix,
    resourceStore: { getSelectedMenu: { id } },
  } = useResourceStore();
  const nonePermissionDs = useMemo(() => new DataSet(OptionsDataSet()), []);

  const linkServiceDs = useMemo(() => new DataSet({
    fields: [
      { name: 'appServiceId', type: 'string', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.app-service` }), required: true },
    ],
  }), [projectId]);

  const linkServiceOptionsDs = useMemo(() => new DataSet({
    transport: {
      read: {
        method: 'get',
        url: `/devops/v1/projects/${projectId}/env/app_services/non_related_app_service?env_id=${id}`,
      },
    },
  }), [projectId]);

  useEffect(() => {
    nonePermissionDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/permission/list_non_related`;
    linkServiceOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/env/app_services/non_related_app_service?env_id=${id}`;
  }, [projectId, id]);
  const value = {
    ...props,
    modalStore,
    nonePermissionDs,
    linkServiceDs,
    linkServiceOptionsDs,
    deployStore,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
})));
