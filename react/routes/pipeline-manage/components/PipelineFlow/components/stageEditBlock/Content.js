import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
import EditColumn from '../eidtColumn';
import { usePipelineStageEditStore } from './stores';

const defaultData = [
  {
    name: '阶段一',
    sequence: 1,
    jobList: [],
  },
];

export default observer(() => {
  const {
    projectId,
    pipelineId,
    editBlockStore,
    stepStore,
    edit,
    appServiceId,
    appServiceName,
  } = usePipelineStageEditStore();
  const {
    setStepData,
    getStepData,
    getStepData2,
    loadData,
    getLoading,
  } = editBlockStore || stepStore;


  useEffect(() => {
    pipelineId ? loadData(projectId, pipelineId) : setStepData(defaultData, edit);
  }, [pipelineId, projectId]);

  function renderColumn() {
    const dataSource = edit ? getStepData2 : getStepData;
    if (dataSource && dataSource.length > 0) {
      return dataSource.map((item, index) => <EditColumn
        columnIndex={index}
        key={item.id}
        {...item}
        edit={edit}
        pipelineId={pipelineId}
        appServiceId={appServiceId}
        appServiceName={appServiceName}
      />);
    }
  }

  return (
    !getLoading ? <div className="c7n-piplineManage-edit">
      {renderColumn()}
    </div> : <Spin />
  );
});
