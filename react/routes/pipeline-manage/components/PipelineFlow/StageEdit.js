import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import EditHeader from './components/eidtHeader';
import StageEditBlock from './components/stageEditBlock';

export default observer((props) => {
  const { id, name, triggerType, stepStore } = props;
  return (
    <div className="c7n-piplineManage">
      <EditHeader type={triggerType} name={name} iconSize={18} />
      <StageEditBlock pipelineId={id} stepStore={stepStore} {...props} />
    </div>
  );
});
