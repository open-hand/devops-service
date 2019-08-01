import React from 'react';
import { StoreProvider } from './stores';
import Cases from './Cases';

export default function Index(props) {
  return (
    <StoreProvider {...props}>
      <Cases />
    </StoreProvider>
  );
}
