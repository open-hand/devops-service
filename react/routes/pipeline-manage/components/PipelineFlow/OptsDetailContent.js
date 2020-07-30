import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
import map from 'lodash/map';
import isEqual from 'lodash/isEqual';
import DetailHeader from './components/detailHeader';
import DetailColumn from './components/detailColumn';
import Loading from '../../../../components/loading';
import EmptyPage from '../../../../components/empty-page';
import { usePipelineManageStore } from '../../stores';

export default observer((props) => {
  const {
    gitlabPipelineId,
    detailStore,
    projectId,
    status: treeStatus,
    treeDs,
    stageRecordVOS: treeStageRecordVOList,
    cdRecordId,
    devopsPipelineRecordRelId,
  } = props;
  const {
    intl: { formatMessage },
    intlPrefix,
    mainStore,
    history,
    location,
  } = usePipelineManageStore();

  const {
    loadDetailData,
    getDetailLoading,
    getDetailData,
  } = detailStore;

  useEffect(() => {
    devopsPipelineRecordRelId && loadDetailData(projectId, devopsPipelineRecordRelId);
  }, [projectId, cdRecordId, devopsPipelineRecordRelId]);

  // stageRecordVOS: 各个详情阶段记录,包括ci和cd的
  // devopsCipiplineVO: 本流水线记录得信息

  const {
    stageRecordVOS,
    ciCdPipelineVO,
    status,
    gitlabPipelineId: pipelineRecordId,
    gitlabTriggerRef,
    commit,
    devopsPipelineRecordRelId: recordDevopsPipelineRecordRelId,
  } = getDetailData;

  useEffect(() => {
    const treeStatusList = map(treeStageRecordVOList || [], 'status');
    const detailStatusList = map(stageRecordVOS || [], 'status');
    if (devopsPipelineRecordRelId === recordDevopsPipelineRecordRelId && (status !== treeStatus || !isEqual(detailStatusList, treeStatusList))) {
      treeDs && treeDs.query();
    }
  }, [pipelineRecordId]);

  const renderStage = () => (
    stageRecordVOS && stageRecordVOS.length > 0 ? stageRecordVOS.map((item) => {
      const { name, status: stageStatus, durationSeconds, sequence, stageId } = item;
      return (
        <DetailColumn
          key={sequence}
          piplineName={name}
          seconds={durationSeconds}
          piplineStatus={stageStatus}
          stageId={stageId}
          history={history}
          location={location}
          {...item}
          {...props}
        />
      );
    }) : (<EmptyPage
      title={formatMessage({ id: status === 'skipped' ? `${intlPrefix}.record.empty.title` : `${intlPrefix}.record.empty.title.other` })}
      describe={formatMessage({ id: status === 'skipped' ? `${intlPrefix}.record.empty.des` : `${intlPrefix}.record.empty.des.other` })}
      access
    />)
  );

  return (
    !getDetailLoading
      ? <div className="c7n-piplineManage">
        <DetailHeader
          devopsPipelineRecordRelId={devopsPipelineRecordRelId}
          appServiceName={ciCdPipelineVO && ciCdPipelineVO.appServiceName}
          appServiceId={ciCdPipelineVO && ciCdPipelineVO.appServiceId}
          aHref={commit && commit.gitlabProjectUrl}
          triggerRef={gitlabTriggerRef}
          status={status}
          mainStore={mainStore}
          projectId={projectId}
        />
        <div className="c7n-piplineManage-detail">
          {renderStage()}
        </div>
      </div> : <Loading display={getDetailLoading} />
  );
});
