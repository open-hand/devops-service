import React from 'react';
import { StoreProvider } from './stores';
import Content from './FlowContent';
import './index.less';

export default (props) => (
  <StoreProvider {...props}>
    <Content />
  </StoreProvider>
);
