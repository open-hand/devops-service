import React from 'react';
import { observer } from 'mobx-react-lite';
import OptsDetailContent from './OptsDetailContent';
import PiplineEdit from './StageEdit';
import { usePipelineFlowStore } from './stores';

export default observer(() => {
  const {
    getSelectedMenu: { parentId },
    getSelectedMenu,
    stepStore,
    detailStore,
    projectId,
    handleRefresh,
    treeDs,
  } = usePipelineFlowStore();

  const renderPipeline = () => (
    parentId ? <OptsDetailContent
      handleRefresh={handleRefresh}
      {...getSelectedMenu}
      treeDs={treeDs}
      projectId={projectId}
      detailStore={detailStore}
    /> : <PiplineEdit {...getSelectedMenu} stepStore={stepStore} />
  );

  return (
    <div className="c7ncd-pipelineManage_flow">
      {renderPipeline()}
    </div>
  );
});
