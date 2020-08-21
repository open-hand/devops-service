import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import FormDataSet from './FormDataSet';
import useStore from './useStore';

const Store = createContext();

export function useRecordDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    children,
    appServiceId,
  } = props;

  const store = useStore();
  const projectFormDs = useMemo(() => new DataSet(FormDataSet({ formatMessage })), []);
  const appFormDs = useMemo(() => new DataSet(FormDataSet({ formatMessage })), []);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    projectFormDs.status = 'loading';
    const res = await store.loadData(projectId, appServiceId);
    if (res) {
      projectFormDs.loadData(res.project);
      appFormDs.loadData(res.app);
      projectFormDs.status = 'ready';
    }
  }

  const value = {
    ...props,
    prefixCls: 'c7ncd-pipelineManage-view-variable',
    intlPrefix: 'c7ncd.pipelineManage',
    projectFormDs,
    appFormDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
