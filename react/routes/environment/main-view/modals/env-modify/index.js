import React from 'react';
import { StoreProvider } from './stores';
import EnvModifyForm from './EnvModifyForm';

export default (props) => <StoreProvider {...props}>
  <EnvModifyForm />
</StoreProvider>;
