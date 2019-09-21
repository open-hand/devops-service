import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { viewTypeMappings, itemTypeMappings } from './mappings';
import TreeDataSet from './TreeDataSet';
import useStore from './useStore';

const Store = createContext();

export function useResourceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      intl: { formatMessage },
      AppState: { currentMenuType: { id } },
      children,
    } = props;
    const resourceStore = useStore();
    const viewType = resourceStore.getViewType;
    const viewTypeMemo = useMemo(() => viewTypeMappings, []);
    const itemTypes = useMemo(() => itemTypeMappings, []);
    const treeDs = useMemo(() => new DataSet(TreeDataSet({ store: resourceStore, type: viewType, projectId: id, formatMessage })), [viewType]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      permissions: [
        'devops-service.devops-environment.listEnvTree',
        'devops-service.devops-environment.listResourceEnvTree',
      ],
      viewTypeMappings: viewTypeMemo,
      itemTypes,
      resourceStore,
      treeDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }),
));
