import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content } from '@choerodon/boot';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar from '../../tool-bar';  
import Branch from '../../../branch';
import MergeRequest from '../../../merge-request';
import '../index.less';

const CodeManagerMergeRequest = observer(props => <TabPage>
  <CodeManagerToolBar name="CodeManagerMergeRequest" />
  <CodeManagerHeader />
  <Content className="c7ncd-code-manager-content">
    <MergeRequest />
  </Content>
</TabPage>);

export default CodeManagerMergeRequest;
