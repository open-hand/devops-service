import React, { Fragment, useRef, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Page, Header, Breadcrumb, Content } from '@choerodon/boot';
import { Button, Modal } from 'choerodon-ui/pro';
import PipelineTree from './components/PipelineTree';
import PipelineFlow from './components/PipelineFlow';
import DragBar from '../../components/drag-bar';
import PipelineCreate from './components/PipelineCreate';
import RecordDetail from './components/record-detail';
import { usePipelineManageStore } from './stores';

import './index.less';

const recordDetailKey = Modal.key();
const modalStyle = {
  width: 380,
};

const PipelineManage = observer((props) => {
  const handleCreatePipeline = () => {
    Modal.open({
      key: Modal.key(),
      title: '创建流水线',
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      drawer: true,
      children: <PipelineCreate />,
      okText: '创建',
    });
  };
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    mainStore,
  } = usePipelineManageStore();
  const rootRef = useRef(null);

  const { getSelectedMenu } = mainStore;

  function openRecordDetail() {
    const { id } = getSelectedMenu;
    Modal.open({
      key: recordDetailKey,
      style: modalStyle,
      title: formatMessage({ id: `${intlPrefix}.record.detail.title` }, { id }),
      children: <RecordDetail recordId={id} intlPrefix={intlPrefix} />,
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  function getButtons() {
    const { parentId, status } = getSelectedMenu;
    if (!parentId) {
      return <Button icon="playlist_add">{formatMessage({ id: 'save' })}</Button>;
    } else {
      let btn;
      switch (status) {
        case 'running':
        case 'pending':
          btn = <Button icon="power_settings_new">{formatMessage({ id: `${intlPrefix}.execute.cancel` })}</Button>;
          break;
        case 'canceled':
        case 'failed':
          btn = <Button icon="power_settings_new">{formatMessage({ id: `${intlPrefix}.execute.retry` })}</Button>;
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
    <Page className="pipelineManage_page">
      <Header title="流水线">
        <Button onClick={handleCreatePipeline} icon="playlist_add">创建流水线</Button>
        {getButtons()}
        <Button icon="refresh">{formatMessage({ id: 'refresh' })}</Button>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-content`}>
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
            <PipelineFlow />
          </div>
        </div>
      </Content>
    </Page>
  );
});

export default PipelineManage;
