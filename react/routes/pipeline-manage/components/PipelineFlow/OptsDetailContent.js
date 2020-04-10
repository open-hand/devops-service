import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
import DetailHeader from './components/detailHeader';
import DetailColumn from './components/detailColumn';

export default observer((props) => {
  const {
    gitlabPipelineId,
    detailStore,
    projectId,
    status: treeStatus,
    treeDs,
  } = props;

  const {
    loadDetailData,
    getDetailLoading,
    getDetailData,
  } = detailStore;

  useEffect(() => {
    loadDetailData(projectId, gitlabPipelineId);
  }, [projectId, gitlabPipelineId]);

  // stageRecordVOList: 各个详情阶段记录
  // devopsCipiplineVO: 本流水线记录得信息
   
  const {
    stageRecordVOList, devopsCiPipelineVO, status,
  } = getDetailData;

  useEffect(() => {
    if (status !== treeStatus) {
      treeDs && treeDs.query();
    }
  }, [treeStatus]);

  const renderStage = () => (
    stageRecordVOList && stageRecordVOList.length > 0 ? stageRecordVOList.map((item) => {
      const { name, status: stageStatus, durationSeconds, sequence } = item;
      return (
        <DetailColumn
          key={sequence}
          piplineName={name}
          seconds={durationSeconds}
          piplineStatus={stageStatus}
          {...item}
          {...props}
        />
      );
    }) : '暂无数据....'
  );

  return (
    !getDetailLoading
      ? <div className="c7n-piplineManage">
        <DetailHeader
          gitlabPipelineId={gitlabPipelineId}
          parentName={devopsCiPipelineVO && devopsCiPipelineVO.name}
          status={status}
        />
        <div className="c7n-piplineManage-detail">
          {renderStage()}
        </div>
      </div> : <Spin />
  );
});
