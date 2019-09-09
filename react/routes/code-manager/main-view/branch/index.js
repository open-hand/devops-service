import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/master';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';
import Branch from '../../contents/branch';


import '../index.less';

const CodeManagerBranch = observer((props) => <TabPage>
  <CodeManagerToolBar name="CodeManagerBranch" key="CodeManagerBranch" />
  <CodeManagerHeader />
  <SelectApp />
  <Content className="c7ncd-code-manager-content">
    <Branch />
  </Content>
</TabPage>);

export default CodeManagerBranch;
