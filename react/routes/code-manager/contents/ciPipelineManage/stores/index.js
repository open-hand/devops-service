import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';

import { useCodeManagerStore } from '../../../stores';
import CiTableDataSet from './CiTableDataSet';
import useStore from './useStore';

const Store = createContext();

export function usePipelineStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const {
    AppState: { currentMenuType: { projectId, id: organizationId } },
    intl: { formatMessage },
    children,
  } = props;

  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  const appServiceId = selectAppDs.current.get('appServiceId');
  const appServiceData = appServiceDs.toData();

  const ciTableDS = useMemo(() => new DataSet(CiTableDataSet(formatMessage)), [projectId]);

  const pipelineActionStore = useStore();

  useEffect(() => {
    appServiceId ? ciTableDS.transport.read.url = `/devops/v1/projects/${projectId}/pipeline/page_by_options?app_service_id=${appServiceId}` : '';
  }, [appServiceId, projectId]);

  const value = {
    ...props,
    ciTableDS,
    pipelineActionStore,
    organizationId,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
})));
