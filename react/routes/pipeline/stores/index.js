import React, { createContext, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import PipelineStore from '../pipeline-table/stores';

const Store = createContext();


const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;

    const value = {
      ...props,
      PipelineStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));

export default StoreProvider;
