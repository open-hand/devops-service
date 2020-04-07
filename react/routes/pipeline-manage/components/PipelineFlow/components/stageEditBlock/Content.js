import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import EditColumn from '../eidtColumn';
import { usePipelineStageEditStore } from './stores';

const data = [
  {
    stepName: '构建',
    id: 1213,
    stepTasks: [
      {
        taskName: '构建maven',
        id: 1213131,
      },
      {
        taskName: '构建maven',
        id: 12131,
      },
    ],
  },
  {
    stepName: '代码检查',
    id: 344,
    stepTasks: [
      {
        taskName: 'maven',
        id: 3431,
      },
      {
        taskName: 'maven',
        id: 8921,
      },
    ],
  },
];

export default observer(() => {
  const {
    stepStore: {
      setStepData, getStepData,
    },
  } = usePipelineStageEditStore();

  useEffect(() => {
    setStepData(data);
  }, []);

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
