import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/master';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar from '../../tool-bar';  
import AppTag from '../../appTag';

import '../index.less';

const CodeManagerAppTag = observer(props => <TabPage>
  <CodeManagerToolBar name="CodeManagerAppTag" key="CodeManagerAppTag" />
  <CodeManagerHeader />
  <Content className="c7ncd-code-manager-content">
    <AppTag />
  </Content>
</TabPage>);

export default CodeManagerAppTag;
