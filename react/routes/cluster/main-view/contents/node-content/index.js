import React from 'react';
import { NodeContentStoreProvider } from './stores';
import Content from './Content';

export default (props) => (
  <NodeContentStoreProvider {...props}>
    <Content />
  </NodeContentStoreProvider>
);
