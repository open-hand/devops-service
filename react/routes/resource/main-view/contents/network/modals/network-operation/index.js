import React from 'react';
import { NetWorkStoreProvider } from './stores';
import FormContent from './network-form';

function NetWork(props) {
  return (
    <NetWorkStoreProvider {...props}> 
      <FormContent />
    </NetWorkStoreProvider>
  );
}

export default NetWork;
