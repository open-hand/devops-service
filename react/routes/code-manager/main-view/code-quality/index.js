import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar from '../../tool-bar';  
import CodeQualityContent from '../../../codeQuality';
import '../index.less';

const CodeQuality = observer(() => <TabPage>
  <CodeManagerToolBar name="CodeQuality" key="CodeQuality" />
  <CodeManagerHeader />
  <Content className="c7ncd-code-manager-content">
    <CodeQualityContent />
  </Content>
</TabPage>);

export default CodeQuality;
