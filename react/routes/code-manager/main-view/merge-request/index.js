import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content } from '@choerodon/boot';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';  
import MergeRequest from '../../contents/merge-request';
import '../index.less';

const CodeManagerMergeRequest = observer((props) => <TabPage>
  <CodeManagerToolBar name="CodeManagerMergeRequest" />
  <CodeManagerHeader />
  <SelectApp />
  <MergeRequest />
</TabPage>);

export default CodeManagerMergeRequest;
