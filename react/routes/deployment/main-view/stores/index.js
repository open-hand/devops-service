import React, { createContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import MainViewStore from './MainViewStore';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { children } = props;

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      podColor: {
        RUNNING_COLOR: '#0bc2a8',
        PADDING_COLOR: '#fbb100',
      },
      instanceView: {
        ENV_ITEM: 'environment',
        APP_ITEM: 'application',
        IST_ITEM: 'instance',
      },
      viewType: {
        IST_VIEW_TYPE: 'instance',
        RES_VIEW_TYPE: 'resource',
      },
      store: new MainViewStore(),
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
