import React from 'react';
import { NetWorkStoreProvider } from './stores';
import FormContent from './FormContent';

function NetWork(props) {
  return (
    <NetWorkStoreProvider {...props}> 
      <FormContent />
    </NetWorkStoreProvider>
  );
}

export default NetWork;
