import React from 'react';
import { observer, inject } from 'mobx-react';
import { PageWrap, PageTab } from '@choerodon/boot';
import CertificateTable from './certificate-table';


const MainView = observer(() => <PageWrap noHeader={[]}>
  <PageTab title="证书列表" tabKey="key1" component={CertificateTable} />
</PageWrap>);


export default MainView;
