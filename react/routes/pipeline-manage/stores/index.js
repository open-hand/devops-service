import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import TreeDataSet from './TreeDataSet';

const Store = createContext();

export function usePipelineManageStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
  } = props;

  const mainStore = useStore();
  const treeDs = useMemo(() => new DataSet(TreeDataSet({ mainStore })), []);

  const data = useMemo(() => [
    {
      id: 1,
      name: 'workflow1',
      appServiceName: 'DevOps服务',
      updateDate: '2018-07-10 09:13:42',
      status: 'success',
      active: true,
      type: 'auto',
    },
    {
      id: 2,
      name: 'workflow2',
      appServiceName: 'base服务',
      updateDate: '2020-03-10 09:13:42',
      status: 'failed',
      active: false,
      type: 'auto',
    },
    {
      id: 109727,
      parentId: 1,
      updateDate: '2020-03-10 09:13:42',
      status: 'failed',
    },
    {
      id: 109725,
      parentId: 1,
      updateDate: '2020-03-30 09:13:42',
      status: 'failed',
    },
  ], []);

  useEffect(() => {
    treeDs.loadData(data);
  }, []);

  const value = {
    ...props,
    prefixCls: 'c7ncd-pipelineManage',
    mainStore,
    treeDs,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
