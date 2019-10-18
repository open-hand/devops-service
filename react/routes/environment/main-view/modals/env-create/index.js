import React from 'react';
import { StoreProvider } from './stores';
import EnvCreateForm from './EnvCreateForm';

export default ({ modal, refresh, intlPrefix, ...props }) => <StoreProvider {...props}>
  <EnvCreateForm modal={modal} refresh={refresh} intlPrefix={intlPrefix} />
</StoreProvider>;
