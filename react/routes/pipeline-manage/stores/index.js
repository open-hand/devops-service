import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { Modal } from 'choerodon-ui/pro';
import useStore from './useStore';
import useEditBlockStore from './useEditBlockStore';
import useDetailStore from './useDetailStore';
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

  function handleSelect(record, store, editBlockStore, previous) {
    const { getHasModify, setHasModify } = editBlockStore;
    if (record) {
      const data = record.toData();
      if (getHasModify(false)) {
        Modal.open({
          key: Modal.key(),
          title: '保存提示',
          children: '您的修改尚未保存，确定要离开吗?',
          onOk: () => {
            store.setSelectedMenu(data);
            record.isSelected = true;
            setHasModify(false, false);
          },
          onCancel: () => { previous.isSelected = true; record.isSelected = false; },
        });
      } else {
        store.setSelectedMenu(data);
        // setHasModify(false, false);
      }
    }
  }

  const mainStore = useStore();
  const editBlockStore = useEditBlockStore();
  const detailStore = useDetailStore();
  const treeDs = useMemo(() => new DataSet(TreeDataSet({ projectId, mainStore, editBlockStore, handleSelect })), [projectId]);

  useEffect(() => {
    // 处理消息铃铛中审核任务的跳转
    const pattern = new URLSearchParams(window.location.hash);
    const newPipelineId = pattern.get('pipelineId');
    const newPipelineIdRecordId = pattern.get('pipelineIdRecordId');

    if (newPipelineId && newPipelineIdRecordId) {
      mainStore.setSelectedMenu({
        key: `${newPipelineId}-${newPipelineIdRecordId}`,
        parentId: newPipelineId,
        devopsPipelineRecordRelId: newPipelineIdRecordId,
      });
      mainStore.setExpandedKeys([newPipelineId]);
    }
    treeDs.query();
  }, []);

  const value = {
    ...props,
    prefixCls: 'c7ncd-pipelineManage',
    intlPrefix: 'c7ncd.pipelineManage',
    permissions: [
      'choerodon.code.project.develop.ci-pipeline.ps.default',
    ],
    mainStore,
    treeDs,
    detailStore,
    editBlockStore,
    projectId,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
