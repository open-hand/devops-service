import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import OptsDetailContent from './components/optsDetailContent';
import PiplineEdit from './components/piplineStageEdit';

export default observer(() => {
  useEffect(() => {

  }, []);

  return (
    <div className="c7ncd-pipelineManage_flow">
      <PiplineEdit />
    </div>
  );
});
