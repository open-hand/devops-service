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
    AppState: { currentMenuType: { projectId } },
    children,
  } = props;

  const mainStore = useStore();
  const editBlockStore = useEditBlockStore();
  const treeDs = useMemo(() => new DataSet(TreeDataSet({ projectId, mainStore })), [projectId]);

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
