import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/master';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';  
import CodeManagerCiPipelineManage from '../../contents/ciPipelineManage';
import '../index.less';

const CiPipelineManage = observer((props) => <TabPage>
  <CodeManagerToolBar name="CodeManagerCiPipelineManage" key="CodeManagerCiPipelineManage" />
  <CodeManagerHeader />
  <SelectApp />
  <Content className="c7ncd-code-manager-content">
    <CodeManagerCiPipelineManage />
  </Content>
</TabPage>);

export default CiPipelineManage;
