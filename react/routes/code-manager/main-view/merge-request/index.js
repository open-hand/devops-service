import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content } from '@choerodon/master';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';  
import MergeRequest from '../../contents/merge-request';
import '../index.less';

const CodeManagerMergeRequest = observer((props) => <TabPage>
  <CodeManagerToolBar name="CodeManagerMergeRequest" />
  <CodeManagerHeader />
  <SelectApp />
  <Content className="c7ncd-code-manager-content">
    <MergeRequest />
  </Content>
</TabPage>);

export default CodeManagerMergeRequest;
