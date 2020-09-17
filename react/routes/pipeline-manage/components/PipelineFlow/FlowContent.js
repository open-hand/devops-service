import React from 'react';
import { observer } from 'mobx-react-lite';
import OptsDetailContent from './OptsDetailContent';
import PipelineDetail from './components/pipeline-detail';
import { usePipelineFlowStore } from './stores';

export default observer(() => {
  const {
    handleRefresh,
    getSelectedMenu,
  } = usePipelineFlowStore();

  const {
    parentId,
  } = getSelectedMenu;

  const renderPipeline = () => (
    parentId ? (
      <OptsDetailContent
        handleRefresh={handleRefresh}
      />
    ) : <PipelineDetail />
  );
  return (
    <div className="c7ncd-pipelineManage_flow">
      {renderPipeline()}
    </div>
  );
});
