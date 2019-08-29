import React from 'react';
import { StoreProvider } from './stores';
import EnvCreateForm from './EnvCreateForm';

export default ({ modal, refresh, ...props }) => <StoreProvider {...props}>
  <EnvCreateForm modal={modal} refresh={refresh} />
</StoreProvider>;
