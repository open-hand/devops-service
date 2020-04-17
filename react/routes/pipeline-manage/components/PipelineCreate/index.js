import React from 'react';
import { StoreProvider } from './stores';
import PipelineCreate from './PipelineCreate';

export default (props) => (
  <StoreProvider {...props}>
    <PipelineCreate />
  </StoreProvider>
);
