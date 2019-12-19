import React from 'react';
import CodeQualityComponent from './CodeQuality';
import { CodeQualityStoreProvider } from './stores';


export default function AppTag(props) {
  return (
    <CodeQualityStoreProvider {...props}>
      <CodeQualityComponent />
    </CodeQualityStoreProvider>
  );
}
