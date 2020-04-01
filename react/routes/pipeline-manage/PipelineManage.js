import React from 'react';
import { observer } from 'mobx-react-lite';
import { Page, Header, Breadcrumb, Content } from '@choerodon/boot';
import { Button, Modal } from 'choerodon-ui/pro';
import PipelineTree from './components/PipelineTree';
import PipelineFlow from './components/PipelineFlow';
import PipelineCreate from './components/PipelineCreate';

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

  return (
    <Page className="pipelineManage_page">
      <Header title="流水线">
        <Button onClick={handleCreatePipeline} icon="playlist_add">创建流水线</Button>
        <Button icon="find_in_page">流水线记录详情</Button>
        <Button icon="power_settings_new">强制失败</Button>
        <Button icon="refresh">刷新</Button>
      </Header>
      <Breadcrumb />
      <Content className="pipelineManage_content">
        <PipelineTree />
        <PipelineFlow />
      </Content>
    </Page>
  );
});

export default PipelineManage;
