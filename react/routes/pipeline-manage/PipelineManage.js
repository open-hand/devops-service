import React, { Fragment, useRef, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Page, Header, Breadcrumb, Content } from '@choerodon/boot';
import { Button, Modal } from 'choerodon-ui/pro';
import PipelineTree from './components/PipelineTree';
import PipelineFlow from './components/PipelineFlow';
import DragBar from '../../components/drag-bar';
import PipelineCreate from './components/PipelineCreate';
import { usePipelineManageStore } from './stores';

import './index.less';

const PipelineManage = observer((props) => {
  const handleCreatePipeline = () => {
    Modal.open({
      key: Modal.key(),
      title: '创建流水线',
      style: {
        width: '7.4rem',
      },
      drawer: true,
      children: <PipelineCreate />,
      okText: '创建',
    });
  };
  const {
    prefixCls,
    mainStore,
  } = usePipelineManageStore();
  const rootRef = useRef(null);

  const { getSelectedMenu } = mainStore;

  function getButtons() {
    if (!getSelectedMenu.parentId) {
      return <Button icon="playlist_add">保存</Button>;
    } else {
      return (<Fragment>
        <Button icon="find_in_page">流水线记录详情</Button>
        <Button icon="power_settings_new">强制失败</Button>
      </Fragment>);
    }
  }

  return (
    <Page className="pipelineManage_page">
      <Header title="流水线">
        <Button onClick={handleCreatePipeline} icon="playlist_add">创建流水线</Button>
        {getButtons()}
        <Button icon="refresh">刷新</Button>
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
