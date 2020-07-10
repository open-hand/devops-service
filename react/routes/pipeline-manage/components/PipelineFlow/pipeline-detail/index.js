import React, { useEffect, Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import forEach from 'lodash/forEach';
import { Tooltip } from 'choerodon-ui/pro';
import { usePipelineFlowStore } from '../stores';
import StageType from '../components/stage-type';

import './index.less';

const jobTask = {
  build: '构建',
  sonar: '代码检查',
  custom: '自定义',
  chart: '发布Chart',
  cdDeploy: '部署',
  cdHost: '主机部署',
  cdAudit: '人工卡点',
};

export default observer((props) => {
  const {
    projectId,
    stepStore,
    getSelectedMenu: { id, name, appServiceName },
  } = usePipelineFlowStore();

  const [leftLineDom, setLeftLineDom] = useState([]);
  const [rightLineDom, setRightLineDom] = useState([]);

  const {
    getStepData,
    loadData,
  } = stepStore || {};

  useEffect(() => {
    id && loadData(projectId, id);
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

  function getJobTask(type, metadata, iamUserDTOS) {
    if (metadata) {
      const newData = JSON.parse(metadata.replace(/'/g, '"'));
      const { sonarUrl, config, envName, triggerValue = [], triggerType, countersigned } = newData || {};
      let content;
      switch (type) {
        case 'sonar':
          content = <div className="c7ncd-pipeline-detail-job-task-sonar">{sonarUrl}</div>;
          break;
        case 'cdDeploy':
        case 'cdHost':
          content = (
            <div className="c7ncd-pipeline-detail-job-task-deploy">
              {envName ? <span className="c7ncd-pipeline-detail-job-task-deploy-item">部署环境：{envName}</span> : null}
              <span>
                触发分支：
                {triggerType === 'exact_exclude' ? '精确排除<br/>' : ''}
                {triggerValue.join()}
              </span>
            </div>
          );
          break;
        case 'cdAudit':
          content = (
            <div className="c7ncd-pipeline-detail-job-task-deploy">
              <span className="c7ncd-pipeline-detail-job-task-deploy-item">
                审核人员：
                {map(iamUserDTOS || [], ({ loginName, realName, id: userId }, index) => (
                  <span key={userId}>
                    {realName}
                    {index !== iamUserDTOS.length - 1 && ','}
                  </span>
                ))}
              </span>
              <span className="c7ncd-pipeline-detail-job-task-deploy-item">审核模式：{countersigned === 0 ? '或签' : '会签'}</span>
              <span>
                触发分支：
                {triggerType === 'exact_exclude' ? '精确排除<br/>' : ''}
                {triggerValue.join()}
              </span>
            </div>
          );
          break;
        case 'build':
          content = config ? (
            map(config, ({ name: taskName, sequence }) => (
              <div className="c7ncd-pipeline-detail-job-task-item" key={sequence}>
                {taskName}
              </div>
            ))
          ) : null;
          break;
        default:
      }
      return content && <div className="c7ncd-pipeline-detail-job-task">{content}</div>;
    }
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
      <div className="c7ncd-pipeline-detail-title">
        <span>{name}</span>
        <span className="c7ncd-pipeline-detail-title-appService">{appServiceName ? ` (${appServiceName}) ` : ''}</span>
      </div>
      <div className="c7ncd-pipeline-detail-content">
        {map(getStepData, ({ id: stageId, name: stageName, jobList, type: stageType = 'CI', parallel }, stageIndex) => (
          <div className="c7ncd-pipeline-detail-stage" key={`${stageId}-${stageIndex}`}>
            <div className="c7ncd-pipeline-detail-stage-title">
              <Tooltip title={stageName} placement="top">
                <span className="c7ncd-pipeline-detail-stage-title-text">{stageName}</span>
              </Tooltip>
              <div className="c7ncd-pipeline-detail-stage-title-type">
                <StageType type={stageType} parallel={parallel} />
              </div>
            </div>
            <div className="c7ncd-pipeline-detail-stage-line" />
            {stageIndex !== 0 ? (
              <div className="c7ncd-pipeline-detail-stage-arrow">
                {stageType === 'CI' ? (
                  <svg xmlns="http://www.w3.org/2000/svg" width="28" height="9" viewBox="0 0 28 9">
                    <path fill="#6887E8" d="M511.5,131 L520.5,135.5 L511.5,140 L511.5,136 L493,136 L493,135 L511.5,135 L511.5,131 Z" transform="translate(-493 -131)" />
                  </svg>
                ) : (
                  <svg xmlns="http://www.w3.org/2000/svg" width="28" height="9" viewBox="0 0 26 9">
                    <path fill="#F1B42D" d="M917.5,130 L926.5,134.5 L917.5,139 L917.5,135 L913.5,135 L913.5,134 L917.5,134 L917.5,130 Z M905.5,134 L905.5,135 L901.5,135 L901.5,134 L905.5,134 Z M911.5,134 L911.5,135 L907.5,135 L907.5,134 L911.5,134 Z" transform="translate(-901 -130)" />
                  </svg>
                )}
              </div>
            ) : null}
            {map(jobList, ({ id: jobId, type: jobType, name: jobName, metadata, iamUserDTOS }, index) => (
              <div key={`${stageId}-${jobId}`}>
                {index && leftLineDom[stageIndex] ? leftLineDom[stageIndex][index] : null}
                <div className={`c7ncd-pipeline-detail-job c7ncd-pipeline-detail-job-${stageType}`} id={`${id}-${stageIndex}-job-${index}`}>
                  <div className="c7ncd-pipeline-detail-job-title">【{jobTask[jobType]}】{jobName}</div>
                  {jobType !== 'custom' && getJobTask(jobType, metadata, iamUserDTOS)}
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
