import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage } from '@choerodon/boot';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';
import Branch from '../../contents/branch';

import '../index.less';

const CodeManagerBranch = observer((props) => <TabPage>
  <CodeManagerToolBar name="CodeManagerBranch" key="CodeManagerBranch" />
  <CodeManagerHeader />
  <SelectApp />
  <Branch />
</TabPage>);

export default CodeManagerBranch;
