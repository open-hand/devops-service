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
  } = props;

  const detailDs = useMemo(() => new DataSet(DetailDataSet({ formatMessage, intlPrefix, projectId })), [projectId]);

  useEffect(() => {
    detailDs.loadData([{
      pipelineName: 'workflow1',
      appServiceName: 'DevOps服务',
      status: 'pending',
      userName: '李洪',
      userImageUrl: '',
      date: '2019-06-26 20:13:48',
      time: '15分钟',

    }]);
  }, []);

  const value = {
    ...props,
    detailDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
