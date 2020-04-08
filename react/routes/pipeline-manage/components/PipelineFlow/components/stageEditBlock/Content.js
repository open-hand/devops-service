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
    editBlockStore,
    stepStore,
    edit,
  } = usePipelineStageEditStore();

  const { setStepData, getStepData, getStepData2 } = editBlockStore || stepStore;

  useEffect(() => {
    const value = pipelineId ? data : defaultData;
    setStepData(value, edit);
  }, [pipelineId]);

  function renderColumn() {
    const dataSource = edit ? getStepData2 : getStepData;
    if (dataSource.length > 0) {
      return dataSource.map((item, index) => <EditColumn columnIndex={index} key={item.id} {...item} edit={edit} />);
    }
  }

  return (
    <div className="c7n-piplineManage-edit">
      {renderColumn()}
    </div>
  );
});
