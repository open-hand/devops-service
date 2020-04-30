import React from 'react';
import { StoreProvider } from './stores';
import FlowContent from './FlowContent';
import './index.less';

export default (props) => (
  <StoreProvider {...props}>
    <FlowContent />
  </StoreProvider>
);
