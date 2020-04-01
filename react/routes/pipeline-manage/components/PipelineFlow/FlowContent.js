import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import OptsDetailContent from './OptsDetailContent';
import PiplineEdit from './StageEdit';
import { usePipelineFlowStore } from './stores';

export default observer(() => {
  const {
    getSelectedMenu: { parentId },
    getSelectedMenu,
  } = usePipelineFlowStore();

  const renderPipeline = () => (
    parentId ? <OptsDetailContent {...getSelectedMenu} /> : <PiplineEdit {...getSelectedMenu} />
  );

  return (
    <div className="c7ncd-pipelineManage_flow">
      {renderPipeline()}
    </div>
  );
});
