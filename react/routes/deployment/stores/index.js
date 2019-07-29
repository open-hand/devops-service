import React, { createContext, useState, useCallback } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id } }, intl, children } = props;

    const [selectedMenu, setSelectedMenu] = useState({});
    const changeSelected = useCallback((menuId, menuType, parentId) => {
      setSelectedMenu({
        menuId,
        menuType,
        parentId,
      });
    }, []);

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      permissions: [
        'devops-service.application-instance.pageByOptions',
      ],
      selectedMenu,
      changeSelected,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
