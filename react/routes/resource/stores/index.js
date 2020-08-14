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
      AppState: { currentMenuType: { id, organizationId, name: projectName } },
      children,
      location,
    } = props;
    const viewTypeMemo = useMemo(() => viewTypeMappings, []);
    const itemTypes = useMemo(() => itemTypeMappings, []);
    const { viewType: newViewType = viewTypeMemo.IST_VIEW_TYPE } = location.state || {};
    const resourceStore = useStore(newViewType);
    const viewType = resourceStore.getViewType;
    const treeDs = useMemo(() => new DataSet(TreeDataSet({ store: resourceStore, type: viewType, projectId: id, formatMessage, organizationId, projectName })), [viewType, id]);

    useEffect(() => {
      // NOTE: 这里只对部署跳转进来的这一种情况处理，若之后添加新的情况可在此处做
      const { IST_VIEW_TYPE, RES_VIEW_TYPE } = viewTypeMemo;
      if (location.state) {
        const { envId, appServiceId, instanceId } = location.state;
        if (newViewType === IST_VIEW_TYPE) {
          if (instanceId) {
            const parentId = `${envId}**${appServiceId}`;
            resourceStore.setSelectedMenu({
              id: instanceId,
              parentId,
              key: `${parentId}**${instanceId}`,
              itemType: itemTypes.IST_ITEM,
            });
            resourceStore.setExpandedKeys([`${envId}`, `${envId}**${appServiceId}`]);
          } else {
            resourceStore.setSelectedMenu({
              id: envId,
              parentId: '0',
              key: String(envId),
              itemType: itemTypes.ENV_ITEM,
            });
            resourceStore.setExpandedKeys([`${envId}`]);
          }
        } else {
          resourceStore.setSelectedMenu({
            id: 0,
            name: formatMessage({ id: 'instances' }),
            key: `${envId}**instances`,
            isGroup: true,
            itemType: 'group_instances',
            parentId: String(envId),
          });
          resourceStore.setExpandedKeys([`${envId}`]);
        }
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
