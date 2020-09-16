import React, {
  createContext, useCallback, useContext, useMemo,
} from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import { DataSet } from 'choerodon-ui/pro';
import ListDataSet from '@/routes/host-config/stores/ListDataSet';
import { DataSetSelection } from 'choerodon-ui/pro/lib/data-set/enum';
import SearchDataSet from '@/routes/host-config/stores/SearchDataSet';

interface ContextProps {
  prefixCls: string,
  intlPrefix: string,
  formatMessage(arg0: object, arg1?: object): string,
  projectId: number,
  listDs: DataSet,
  searchDs: DataSet,
  hostTabKeys:{
    key:string,
    text:string,
  }[],
}

const Store = createContext({} as ContextProps);

export function useHostConfigStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')((props: any) => {
  const {
    children,
    intl: { formatMessage },
    AppState: { currentMenuType: { projectId } },
  } = props;
  const intlPrefix = 'c7ncd.host.config';

  const statusDs = useMemo(() => new DataSet({
    data: [
      {
        text: formatMessage({ id: 'success' }),
        value: 'success',
      },
      {
        text: formatMessage({ id: 'failed' }),
        value: 'failed',
      },
    ],
    selection: 'single' as DataSetSelection,
  }), []);

  const hostTabKeys = useMemo(() => [
    {
      key: 'test',
      text: '测试主机',
    },
    {
      key: 'deploy',
      text: '部署主机',
    },
  ], []);

  const listDs = useMemo(() => new DataSet(ListDataSet({ projectId })), [projectId]);
  const searchDs = useMemo(() => new DataSet(SearchDataSet({ projectId, statusDs })), [projectId]);

  const refresh = useCallback(async (callback?:CallableFunction) => {
    await listDs.query();
    typeof callback === 'function' && callback();
  }, [listDs]);

  const value = {
    ...props,
    intlPrefix,
    prefixCls: 'c7ncd-host-config',
    formatMessage,
    listDs,
    searchDs,
    hostTabKeys,
    refresh,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
