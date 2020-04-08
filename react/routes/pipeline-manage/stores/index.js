import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import useEditBlockStore from './useEditBlockStore';
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
  const editBlockStore = useEditBlockStore();
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
      hasMore: true,
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
      id: 3,
      name: 'workflow3',
      appServiceName: '平台总前端',
      updateDate: '2020-04-01 15:00:42',
      status: 'running',
      active: true,
      type: 'auto',
    },
    {
      id: 4,
      name: 'workflow4',
      appServiceName: '平台总前端',
      updateDate: '2020-04-01 15:00:42',
      status: 'canceled',
      active: true,
      type: 'manual',
    },
    {
      id: 109727,
      parentId: 1,
      parentName: 'workflow1',
      updateDate: '2020-03-10 09:13:42',
      status: 'failed',
      stages: [
        { status: 'success' },
        { status: 'failed' },
        { status: 'pending' },
        { status: 'running' },
        { status: 'canceled' },
      ],
    },
    {
      id: 109725,
      parentId: 1,
      parentName: 'workflow1',
      updateDate: '2020-03-30 09:13:42',
      status: 'success',
    },
    {
      id: 109726,
      parentId: 3,
      parentName: 'workflow3',
      updateDate: '2020-03-30 09:13:42',
      status: 'pending',
    },
    {
      id: 109724,
      parentId: 3,
      parentName: 'workflow3',
      updateDate: '2020-03-30 09:13:42',
      status: 'running',
    },
  ], []);

  useEffect(() => {
    treeDs.loadData(data);
  }, []);

  const value = {
    ...props,
    prefixCls: 'c7ncd-pipelineManage',
    intlPrefix: 'c7ncd.pipelineManage',
    mainStore,
    treeDs,
    editBlockStore,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
