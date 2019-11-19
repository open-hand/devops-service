import React from 'react';
import RequestPanel from './RequestPanel';

import StoreProvider from './stores';

export default function Request(props) {
  return <StoreProvider {...props}>
    <RequestPanel />
  </StoreProvider>;
}
