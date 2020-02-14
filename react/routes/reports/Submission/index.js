import React from 'react';
import Submission from './Submission';
import { StoreProvider } from './stores';

export default (props) => (
  <StoreProvider {...props}>
    <Submission />
  </StoreProvider>
);
