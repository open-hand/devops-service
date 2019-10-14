import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { axios } from '@choerodon/boot';
import { DataSet } from 'choerodon-ui/pro';
import { useEnvironmentStore } from '../../../../stores';
import TableDataSet from './TableDataSet';
import openWarnModal from '../../../../../../utils/openWarnModal';

const Store = createContext();

export function useEnvGroupStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { intl: { formatMessage }, children, AppState: { currentMenuType: { id: projectId } } } = props;
    const {
      intlPrefix,
      envStore: {
        getSelectedMenu: { id },
      },
      treeDs,
    } = useEnvironmentStore();
    const groupDs = useMemo(() => new DataSet(TableDataSet({ formatMessage, intlPrefix })), []);

    function freshTree() {
      treeDs.query();
    }

    async function checkGroupExist() {
      if (!id) return true;

      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/env_groups/${id}/check`);
        if (typeof res === 'boolean') {
          if (!res) {
            openWarnModal(freshTree, formatMessage);
          }
          return res;
        }
        // 只有请求到false，才返回false
        return true;
      } catch (e) {
        return true;
      }
    }

    useEffect(() => {
      checkGroupExist().then((query) => {
        if (query) {
          const param = typeof id === 'number' && id ? `?group_id=${id}` : '';
          groupDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/list_by_group${param}`;
          groupDs.query();
        }
      });
    }, [id, projectId]);

    const value = {
      ...props,
      groupDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
