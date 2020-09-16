import React from 'react';
import { StoreProvider } from './stores';
import Content from './Content';

import './index.less';

const HostConfigIndex = (props: any) => (
  <StoreProvider {...props}>
    <Content />
  </StoreProvider>
);

export default HostConfigIndex;
