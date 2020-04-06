import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import useStore from '../../../../../stores/useStore';
import useStepStore from './useStore';


const Store = createContext();

export function usePipelineStageEditStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props) => {
  const {
    children,
  } = props;

  const stepStore = useStepStore();

  const value = {
    ...props,
    stepStore,
  };

  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
