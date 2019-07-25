import React, { createContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import DeploymentStore from './DeploymentStore';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { type, id } }, intl, children } = props;
    const value = {
      ...props,
      prefixCls: 'lc-model-list',
      intlPrefix: type === 'organization' ? 'organization.model.list' : 'global.model.list',
      permissions: [
        'low-code-service.model.pagedSearch',
        'low-code-service.model.createModel',
        'low-code-service.model.createBaseOnTable',
        'low-code-service.model.check',
        'low-code-service.model.update',
        'low-code-service.model.delete',
      ],
      publishStatus: {
        PUBLISHED: '已发布',
        UNPUBLISH: '未发布',
        PUBLISHING: '发布中',
      },
      publishColor: {
        UNPUBLISH: '#b5b5b5',
        PUBLISHING: '#4f90fe',
        PUBLISHED: '#00bf96',
      },
      store: new DeploymentStore(),
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
