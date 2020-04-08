import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import EditHeader from './components/eidtHeader';
import StageEditBlock from './components/stageEditBlock';

export default observer((props) => {
  const { id, name, appServiceName, updateDate, status, active, type, stepStore } = props;
  return (
    <div className="c7n-piplineManage">
      <EditHeader type={type} name={name} iconSize={18} />
      <StageEditBlock pipelineId={id} stepStore={stepStore} />
    </div>
  );
});
