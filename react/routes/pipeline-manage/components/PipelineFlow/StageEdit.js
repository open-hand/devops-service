import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
import EditHeader from './components/eidtHeader';
import StageEditBlock from './components/stageEditBlock';


export default observer((props) => {
  const { id, name, appServiceName, appServiceId, updateDate, status, active, projectId, triggerType, stepStore } = props;
  return (
    // !getEditLoading
    <div className="c7n-piplineManage">
      <EditHeader type={triggerType} name={name} iconSize={18} />
      <StageEditBlock pipelineId={id} stepStore={stepStore} appServiceId={appServiceId} projectId={projectId} />
    </div>
  );
});
