import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import UpgradeDataSet from './UpgradeDataSet';
import VersionsDataSet from './VersionsDataSet';
import useStore from './useStore';
import ValueDataSet from './ValueDataSet';

const Store = createContext();

export default Store;

export function useUpgradeStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      children,
      intl: { formatMessage },
      vo: {
        id,
        parentId,
        versionId,
        appServiceId,
      },
      intlPrefix,
    } = props;

    const upgradeStore = useStore();
    const valueDs = useMemo(() => new DataSet(ValueDataSet({ projectId, appServiceInstanceId: id, versionId })), [projectId, id, versionId]);
    const versionsDs = useMemo(() => new DataSet(VersionsDataSet({ formatMessage, intlPrefix, projectId, appServiceId, upgradeStore, versionId })), [projectId, appServiceId]);
    const upgradeDs = useMemo(() => new DataSet(UpgradeDataSet({ formatMessage, intlPrefix, projectId, versionsDs, valueDs })), [projectId]);

    useEffect(() => {
      const record = upgradeDs.current;
      const [envId] = parentId.split('**');
      if (record) {
        record.init({
          appServiceVersionId: versionId,
          type: 'update',
          instanceId: id,
          environmentId: envId,
          appServiceId,
        });
      }
    }, []);

    const value = {
      ...props,
      upgradeDs,
      versionsDs,
      upgradeStore,
      valueDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }),
));
