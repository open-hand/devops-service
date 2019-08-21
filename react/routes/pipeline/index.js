import React from 'react';
import PipelineTable from './pipeline-table';
import StoreProvider from './stores';

export default function Pipeline(props) {
  return <StoreProvider {...props}>
    <PipelineTable />
  </StoreProvider>;
}
