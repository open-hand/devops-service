import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { viewTypeMappings, itemTypeMappings } from './mappings';
import TreeDataSet from './TreeDataSet';
import useStore from './useStore';

const Store = createContext();

export function useResourceStore() {
  return useContext(Store);
}

export const StoreProvider = withRouter(injectIntl(inject('AppState')(
  observer((props) => {
    const {
      intl: { formatMessage },
      AppState: { currentMenuType: { id } },
      children,
      location,
    } = props;
    const resourceStore = useStore();
    const viewType = resourceStore.getViewType;
    const viewTypeMemo = useMemo(() => viewTypeMappings, []);
    const itemTypes = useMemo(() => itemTypeMappings, []);
    const treeDs = useMemo(() => new DataSet(TreeDataSet({ store: resourceStore, type: viewType, projectId: id, formatMessage })), [viewType, id]);

    useEffect(() => {
      // NOTE: 这里只对部署跳转进来的这一种情况处理，若之后添加新的情况可在此处做
      if (location.state) {
        const { envId, appServiceId, instanceId } = location.state;
        const parentId = `${envId}-${appServiceId}`;
        resourceStore.setSelectedMenu({
          id: instanceId,
          parentId,
          key: `${parentId}-${instanceId}`,
          itemType: itemTypes.IST_ITEM,
        });
        resourceStore.setExpandedKeys([`${envId}`, `${envId}-${appServiceId}`]);
      }
    }, []);

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
)));
