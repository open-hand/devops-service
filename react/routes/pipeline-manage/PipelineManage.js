import React, { Fragment, useRef, useMemo, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import { Page, Header, Breadcrumb, Content, Permission } from '@choerodon/boot';
import { Button, Modal } from 'choerodon-ui/pro';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../utils';
import PipelineTree from './components/PipelineTree';
import PipelineFlow from './components/PipelineFlow';
import DragBar from '../../components/drag-bar';
import PipelineCreate from './components/PipelineCreate';
import RecordDetail from './components/record-detail';
import EmptyPage from '../../components/empty-page';
import { usePipelineManageStore } from './stores';

import './index.less';

const recordDetailKey = Modal.key();
const modalStyle = {
  width: 380,
};

const PipelineManage = observer((props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    permissions,
    mainStore,
    editBlockStore,
    detailStore,
    detailStore: {
      loadDetailData, getDetailData,
    },
    editBlockStore: {
      getMainData, loadData,
    },
    treeDs,
    projectId,
  } = usePipelineManageStore();


  const handleCreatePipeline = () => {
    Modal.open({
      key: Modal.key(),
      title: '创建流水线',
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      drawer: true,
      children: <PipelineCreate refreshTree={handleRefresh} editBlockStore={editBlockStore} />,
      okText: '创建',
    });
  };

  const rootRef = useRef(null);

  const { getSelectedMenu } = mainStore;

  async function handleRefresh() {
    await treeDs.query();
    const { id } = getMainData;
    const { parentId } = getSelectedMenu;
    const { gitlabPipelineId } = getDetailData;
    !parentId ? loadData(projectId, id) : loadDetailData(projectId, gitlabPipelineId);
  }

  async function handleSaveEdit() {
    const { id } = getMainData;
    try {
      const res = await axios.put(`/devops/v1/projects/${projectId}/ci_pipelines/${id}`, getMainData);
      if (handlePromptError(res)) {
        loadData(projectId, id);
        return res;
      }
      return false;
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  function openRecordDetail() {
    const { gitlabPipelineId } = getSelectedMenu;
    Modal.open({
      key: recordDetailKey,
      style: modalStyle,
      title: formatMessage({ id: `${intlPrefix}.record.detail.title` }, { id: gitlabPipelineId }),
      children: <RecordDetail pipelineRecordId={gitlabPipelineId} intlPrefix={intlPrefix} />,
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  async function changeRecordExecute(type) {
    const { gitlabProjectId, gitlabPipelineId } = getSelectedMenu;
    const res = await mainStore.changeRecordExecute({
      projectId,
      gitlabProjectId,
      recordId: gitlabPipelineId,
      type,
    });
    if (res) {
      handleRefresh();
    }
  }

  function getButtons() {
    const { parentId, status } = getSelectedMenu;
    if (!parentId) {
      return (
        <Permission service={['devops-service.devops-ci-pipeline.update']}>
          <Button
            icon="playlist_add"
            onClick={handleSaveEdit}
          >
            {formatMessage({ id: 'save' })}
          </Button>
        </Permission>
      );
    } else {
      let btn;
      switch (status) {
        case 'running':
        case 'pending':
          btn = <Permission service={['devops-service.project-pipeline.cancel']}>
            <Button
              icon="power_settings_new"
              onClick={() => changeRecordExecute('cancel')}
            >
              {formatMessage({ id: `${intlPrefix}.execute.cancel` })}
            </Button>
          </Permission>;
          break;
        case 'canceled':
        case 'failed':
          btn = <Permission service={['devops-service.project-pipeline.retry']}>
            <Button
              icon="refresh"
              onClick={() => changeRecordExecute('retry')}
            >
              {formatMessage({ id: `${intlPrefix}.execute.retry` })}
            </Button>
          </Permission>;
          break;
        default:
          break;
      }
      return (<Fragment>
        <Button
          icon="find_in_page"
          onClick={openRecordDetail}
        >
          {formatMessage({ id: `${intlPrefix}.record.detail` })}
        </Button>
        {btn}
      </Fragment>);
    }
  }

  return (
    <Page service={permissions} className="pipelineManage_page">
      <Header title="流水线">
        <Permission service={['devops-service.devops-ci-pipeline.create']}>
          <Button
            onClick={handleCreatePipeline}
            icon="playlist_add"
          >
            {formatMessage({ id: `${intlPrefix}.create` })}
          </Button>
        </Permission>
        {!treeDs.length && treeDs.status === 'ready' ? null : getButtons()}
        <Button
          onClick={handleRefresh}
          icon="refresh"
        >
          {formatMessage({ id: 'refresh' })}
        </Button>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-content`}>
        {!treeDs.length && treeDs.status === 'ready' ? <div className={`${prefixCls}-wrap`}>
          <Suspense fallback={<span />}>
            <EmptyPage
              title={formatMessage({ id: 'empty.title.pipeline' })}
              describe={formatMessage({ id: 'empty.tips.pipeline.owner' })}
              btnText={formatMessage({ id: `${intlPrefix}.create` })}
              onClick={handleCreatePipeline}
              access
            />
          </Suspense>
        </div> : (
          <div
            ref={rootRef}
            className={`${prefixCls}-wrap`}
          >
            <DragBar
              parentRef={rootRef}
              store={mainStore}
            />
            <PipelineTree />
            <div className={`${prefixCls}-main ${prefixCls}-animate`}>
              <PipelineFlow stepStore={editBlockStore} detailStore={detailStore} handleRefresh={handleRefresh} />
            </div>
          </div>
        )}
      </Content>
    </Page>
  );
});

export default PipelineManage;
