import React from 'react';
import { StoreProvider } from './stores';
import PipelineManage from './PipelineManage';

export default (props) => (
  <StoreProvider {...props}>
    <PipelineManage />
  </StoreProvider>
);
