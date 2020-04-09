import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
import { Choerodon } from '@choerodon/boot';
import DetailHeader from './components/detailHeader';
import DetailColumn from './components/detailColumn';
import { handlePromptError } from '../../../../utils';


export default observer((props) => {
  const {
    id,
    parentId,
    updateDate,
    status,
    stages,
    gitlabPipelineId,
    ciPipelineId,
    detailStore,
    projectId,
  } = props;

  const {
    loadDetailData,
    getDetailLoading,
    setDetailLoading,
    setDetailData,
    getDetailData,
  } = detailStore;

  useEffect(() => {
    loadDetailData(projectId, gitlabPipelineId);
  }, [projectId, gitlabPipelineId]);

  const {
    stageRecordVOList, devopsCiPipelineVO,
  } = getDetailData;

  const renderStage = () => (
    stageRecordVOList && stageRecordVOList.length > 0 ? stageRecordVOList.map((item, index) => {
      const { name, status: stageStatus } = item;
      return (
        <DetailColumn piplineName={name} piplineStatus={stageStatus} {...item} {...props} />
      );
    }) : '暂无数据...'
  );

  return (
    !getDetailLoading
      ? <div className="c7n-piplineManage">
        <DetailHeader gitlabPipelineId={gitlabPipelineId} parentName={devopsCiPipelineVO && devopsCiPipelineVO.name} status={status} />
        <div className="c7n-piplineManage-detail">
          {renderStage()}
        </div>
      </div> : <Spin />
  );
});
