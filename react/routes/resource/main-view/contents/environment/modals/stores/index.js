import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import useStore from './useStore';
import OptionsDataSet from './OptionsDataSet';
import { useResourceStore } from '../../../../../stores';

const Store = createContext();

export function useModalStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const modalStore = useStore();
  const {
    AppState: { currentMenuType: { projectId } },
    children,
  } = props;
  const {
    resourceStore: { getSelectedMenu: { id } },
  } = useResourceStore();

  const nonePermissionDs = useMemo(() => new DataSet(OptionsDataSet()), []);

  useEffect(() => {
    nonePermissionDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/permission/list_non_related`;
  }, [projectId, id]);
  const value = {
    ...props,
    modalStore,
    nonePermissionDs,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
})));
