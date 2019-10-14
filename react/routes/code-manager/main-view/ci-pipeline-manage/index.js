import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/boot';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';  
import CodeManagerCiPipelineManage from '../../contents/ciPipelineManage';
import '../index.less';

const CiPipelineManage = observer((props) => <TabPage>
  <CodeManagerToolBar name="CodeManagerCiPipelineManage" key="CodeManagerCiPipelineManage" />
  <CodeManagerHeader />
  <SelectApp />
  <CodeManagerCiPipelineManage />
</TabPage>);

export default CiPipelineManage;
