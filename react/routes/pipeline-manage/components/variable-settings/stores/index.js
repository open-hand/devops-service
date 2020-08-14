import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import FormDataSet from './FormDataSet';

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
    appServiceId,
    store,
    refresh,
  } = props;

  const formDs = useMemo(() => new DataSet(FormDataSet({ formatMessage, intlPrefix, projectId, appServiceId, store, refresh })), [projectId, appServiceId]);

  // useEffect(() => {
  //   formDs.loadData([
  //     { key: 'aaa', value: 'aafsa' },
  //     { key: 'SDF', value: 'aaasfdasfdfsa' },
  //     { key: 'aaASa', value: 'aafadssa' },
  //   ]);
  // }, []);

  const value = {
    ...props,
    prefixCls: 'c7ncd-pipelineManage-variable-settings',
    formDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
