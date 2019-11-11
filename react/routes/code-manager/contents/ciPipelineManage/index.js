import React from 'react';
import { StoreProvider } from './stores';
import CiPipelineTable from './CiPipelineTable';

export default (props) => (
  <StoreProvider {...props}>
    <CiPipelineTable />
  </StoreProvider>
);
