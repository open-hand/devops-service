import React from 'react';
import { StoreProvider } from './stores';
import EnvModifyForm from './EnvModifyForm';

export default ({ modal, refresh, ...props }) => <StoreProvider {...props}>
  <EnvModifyForm modal={modal} refresh={refresh} />
</StoreProvider>;
