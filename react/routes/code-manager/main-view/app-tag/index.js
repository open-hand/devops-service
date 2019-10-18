import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage } from '@choerodon/boot';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';
import AppTag from '../../contents/appTag';
import '../index.less';

const CodeManagerAppTag = observer(() => <TabPage>
  <CodeManagerToolBar name="CodeManagerAppTag" key="CodeManagerAppTag" />
  <CodeManagerHeader />
  <SelectApp />
  <AppTag />
</TabPage>);

export default CodeManagerAppTag;
