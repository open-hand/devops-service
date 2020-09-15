import React from 'react';
import { StoreProvider } from './stores';
import Content from './Content';

import './index.less';

const CreateHostIndex = (props: any) => (
  <StoreProvider {...props}>
    <Content />
  </StoreProvider>
);

export default CreateHostIndex;
