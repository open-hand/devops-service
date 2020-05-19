import React, { useEffect, Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import forEach from 'lodash/forEach';
import reduce from 'lodash/reduce';
import { Spin } from 'choerodon-ui';
import { usePipelineFlowStore } from '../stores';

import './index.less';

const jobTask = {
  build: '构建',
  sonar: '代码检查',
  custom: '自定义',
};


export default observer((props) => {
  const {
    projectId,
    stepStore,
    getSelectedMenu: { id, name },
  } = usePipelineFlowStore();

  const [leftLineDom, setLeftLineDom] = useState([]);
  const [rightLineDom, setRightLineDom] = useState([]);

  const {
    getStepData,
    loadData,
  } = stepStore || {};

  useEffect(() => {
    loadData(projectId, id);
  }, [id, projectId]);

  useEffect(() => {
    const leftList = [];
    const rightList = [];
    forEach(getStepData, ({ jobList }, stageIndex) => {
      forEach(jobList, (data, index) => {
        const leftItem = (
          <div
            className="c7ncd-pipeline-detail-job-rect-left"
            style={getJobRectStyle(stageIndex, index)}
          />
        );
        const rightItem = (
          <div
            className="c7ncd-pipeline-detail-job-rect-right"
            style={getJobRectStyle(stageIndex, index)}
          />
        );
        if (leftList[stageIndex]) {
          leftList[stageIndex].push(leftItem);
          rightList[stageIndex].push(rightItem);
        } else {
          leftList[stageIndex] = [leftItem];
          rightList[stageIndex] = [rightItem];
        }
      });
    });
    setLeftLineDom(leftList);
    setRightLineDom(rightList);
  }, [getStepData]);

  function getJobTask(metadata) {
    const newData = JSON.parse(metadata.replace(/'/g, '"'));
    const { type, sonarUrl, config } = newData || {};
    let content;
    if (type === 'sonar') {
      content = <div className="c7ncd-pipeline-detail-job-task-sonar">{sonarUrl}</div>;
    } else {
      content = (
        map(config, ({ name: taskName, sequence }) => (
          <div className="c7ncd-pipeline-detail-job-task-item" key={sequence}>
            {taskName}
          </div>
        ))
      );
    }
    return <div className="c7ncd-pipeline-detail-job-task">{content}</div>;
  }

  function getJobRectStyle(stageIndex, index) {
    let sum = 0;
    for (let i = 0; i < index; i++) {
      sum += document.getElementById(`${id}-${stageIndex}-job-${i}`) ? document.getElementById(`${id}-${stageIndex}-job-${i}`).offsetHeight + 32 : 0;
    }
    return { height: sum };
  }

  return (
    <div className="c7ncd-pipeline-detail">
      <div className="c7ncd-pipeline-detail-title">{name}</div>
      <div className="c7ncd-pipeline-detail-content">
        {map(getStepData, ({ id: stageId, name: stageName, jobList }, stageIndex) => (
          <div className="c7ncd-pipeline-detail-stage" key={stageId}>
            <div className="c7ncd-pipeline-detail-stage-title">{stageName}</div>
            <div className="c7ncd-pipeline-detail-stage-line" />
            {stageIndex !== getStepData.length - 1 ? <div className="c7ncd-pipeline-detail-stage-arrow">→</div> : null}
            {map(jobList, ({ id: jobId, type: jobType, name: jobName, metadata }, index) => (
              <div key={`${stageId}-${jobId}`}>
                {index && leftLineDom[stageIndex] ? leftLineDom[stageIndex][index] : null}
                <div className={`c7ncd-pipeline-detail-job ${jobType === 'custom' ? 'c7ncd-pipeline-detail-job-custom' : ''}`} id={`${id}-${stageIndex}-job-${index}`}>
                  <div className="c7ncd-pipeline-detail-job-title">【{jobTask[jobType]}】{jobName}</div>
                  {jobType !== 'custom' && getJobTask(metadata)}
                </div>
                {index && stageIndex !== getStepData.length - 1 && rightLineDom[stageIndex] ? rightLineDom[stageIndex][index] : null}
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
});
