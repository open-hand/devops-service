import React from 'react';
import { observer } from 'mobx-react-lite';
import { TabPage, Content, Header, Breadcrumb } from '@choerodon/boot';
// import CertificateManagerHeader from '../../header';
// import CertificateToolBar from '../../tool-bar';  
import Certificate from '../../../certificate';

import './index.less';

const CertificateTable = observer(props => <TabPage>
  {/* <CertificateToolBar />
  <CertificateManagerHeader /> */}
  <Content className="c7ncd-certificate-manager-content">
    <Certificate />
  </Content>
</TabPage>);

export default CertificateTable;
