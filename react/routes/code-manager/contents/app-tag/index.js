import React from 'react';
import AppTagComponent from './AppTag';
import { AppTagStoreProvider } from './stores';


export default function AppTag(props) {
  return (
    <AppTagStoreProvider {...props}>
      <AppTagComponent />
    </AppTagStoreProvider>
  );
}
