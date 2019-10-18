import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import CodeManagerHeader from '../../header';
import CodeManagerToolBar, { SelectApp } from '../../tool-bar';  
import CodeQualityContent from '../../contents/codeQuality';
import DevPipelineStore from '../../stores/DevPipelineStore';
import Loading from '../../../../components/loading';
import '../index.less';

const CodeQuality = observer(() => <TabPage>
  <CodeManagerToolBar name="CodeQuality" key="CodeQuality" />
  <CodeManagerHeader />
  <SelectApp />
  <CodeQualityContent />
</TabPage>);

export default CodeQuality;
