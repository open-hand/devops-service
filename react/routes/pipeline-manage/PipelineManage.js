import React from 'react';
import { observer } from 'mobx-react-lite';
import { Page, Header, Breadcrumb, Content } from '@choerodon/boot';
import { Button } from 'choerodon-ui/pro';
import PipelineTree from './components/PipelineTree';
import PipelineFlow from './components/PipelineFlow';

import './index.less';

const PipelineManage = observer((props) => (
  <Page className="pipelineManage_page">
    <Header title="流水线">
      <Button icon="playlist_add">创建流水线</Button>
      <Button icon="playlist_add">流水线记录详情</Button>
      <Button icon="playlist_add">强制失败</Button>
      <Button icon="playlist_add">刷新</Button>
    </Header>
    <Breadcrumb />
    <Content className="pipelineManage_content">
      <PipelineTree />
      <PipelineFlow />
    </Content>
  </Page>
));

export default PipelineManage;
