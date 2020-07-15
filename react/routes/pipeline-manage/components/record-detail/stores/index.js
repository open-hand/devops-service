import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import DetailDataSet from './DetailDataSet';

const Store = createContext();

export function useRecordDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    intlPrefix,
    children,
    pipelineRecordId,
    store,
    refresh,
  } = props;
  const detailDs = useMemo(() => new DataSet(DetailDataSet({ formatMessage, intlPrefix, projectId, pipelineRecordId, store, refresh })), [projectId, pipelineRecordId]);

  const value = {
    ...props,
    prefixCls: 'c7ncd-pipelineManage-record-detail',
    detailDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
