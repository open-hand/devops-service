import React from 'react';
import { NodeContentStoreProvider } from './stores';
import NodeContent from './NodeContent';

export default (props) => (
  <NodeContentStoreProvider {...props}>
    <NodeContent />
  </NodeContentStoreProvider>
);
