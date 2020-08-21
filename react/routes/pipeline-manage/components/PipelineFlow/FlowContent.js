import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import OptsDetailContent from './OptsDetailContent';
import PipelineDetail from './pipeline-detail';
import { usePipelineFlowStore } from './stores';

export default observer(() => {
  const {
    getSelectedMenu: { parentId },
    getSelectedMenu,
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
    /> : <PipelineDetail />
  );
  return (
    <div className="c7ncd-pipelineManage_flow">
      {renderPipeline()}
    </div>
  );
});
