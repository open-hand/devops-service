import React, { useContext, useMemo, useEffect, createContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { useClusterStore } from '../../../../stores';


const NodeContentStore = createContext();

const useNodeContentStore = () => useContext(NodeContentStore);

const NodeContentStoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const { intl: { formatMessage }, AppState: { currentMenuType: { id: projectId } }, children } = props;
  const clusterStore = useClusterStore();
  const value = {
    formatMessage,
    projectId,
    clusterStore,
  };

  return (
    <NodeContentStore.Provider value={value}>
      {children}
    </NodeContentStore.Provider>
  );
})));

export {
  useNodeContentStore,
  NodeContentStoreProvider,
};
