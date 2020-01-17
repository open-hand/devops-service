import React from 'react/index';
import { StoreProvider } from './stores';
import Content from './Content';

export default props => (
  <StoreProvider {...props}>
    <Content />
  </StoreProvider>
);
