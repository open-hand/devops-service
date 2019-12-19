import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';  
import CodeQualityContent from './content';
import '../index.less';

const CodeQuality = observer(() => <TabPage>
  <CodeManagerToolBar name="CodeQuality" key="CodeQuality" />
  <CodeManagerHeader />
  <SelectApp />
  <CodeQualityContent />
</TabPage>);

export default CodeQuality;
