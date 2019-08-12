import React from 'react';
import TreeItem from './TreeItem';
import { StoreProvider } from './stores';

export default (props) => {
  const { record, search, ...restProps } = props;
  return <StoreProvider {...restProps}>
    <TreeItem record={record} search={search} />
  </StoreProvider>;
};
