import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import EditColumn from '../eidtColumn';
import { usePipelineStageEditStore } from './stores';


const data = [
  {
    name: '构建',
    sequence: 1,
    jobList: [],
  },
  {
    name: '代码检查',
    sequence: 2,
    jobList: [],
  },
];

const defaultData = [
  {
    name: '阶段一',
    sequence: 1,
    jobList: [],
  },
];

export default observer(() => {
  const {
    pipelineId,
    stepStore: {
      setStepData, getStepData,
    },
  } = usePipelineStageEditStore();

  useEffect(() => {
    setStepData(pipelineId ? data : defaultData);
  }, [pipelineId]);

  function renderColumn() {
    if (getStepData.length > 0) {
      return getStepData.map((item, index) => <EditColumn columnIndex={index} key={item.id} {...item} />);
    }
  }

  return (
    <div className="c7n-piplineManage-edit">
      {renderColumn()}
    </div>
  );
});
