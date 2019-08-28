import React from 'react';
import { StoreProvider } from './stores';
import EnvCreateForm from './EnvCreateForm';

export default ({ modal, treeDs, ...props }) => <StoreProvider {...props}>
  <EnvCreateForm modal={modal} treeDs={treeDs} />
</StoreProvider>;
