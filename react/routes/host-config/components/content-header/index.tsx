import React, { useEffect, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Button, Form, Icon, Select, TextField, DataSet,
} from 'choerodon-ui/pro';
import {
  ButtonColor, FuncType, LabelLayoutType,
} from '../../../../interface';
import { useHostConfigStore } from '../../stores';
import HostPick from '../host-pick';

const ContentHeader: React.FC<any> = observer((): any => {
  const {
    prefixCls,
    formatMessage,
    searchDs,
    hostTabKeys,
    listDs,
    mainStore,
    HAS_BASE_PRO,
    statusDs,
  } = useHostConfigStore();

  const searchArr = useMemo(() => ([
    {
      text: formatMessage({ id: 'success' }),
      value: 'success',
    },
    {
      text: formatMessage({ id: 'failed' }),
      value: 'failed',
    },
    {
      text: formatMessage({ id: 'connecting' }),
      value: 'operating',
    },
    {
      text: formatMessage({ id: 'occupied' }),
      value: 'occupied',
    },
  ]), []);

  const getSearchArr = ():object[] => {
    const isTest = mainStore.getCurrentTabKey === 'distribute_test';
    return isTest ? searchArr : searchArr.slice(0, searchArr.length - 1);
  };

  useEffect(() => {
    statusDs && statusDs.loadData(getSearchArr());
  }, [mainStore.getCurrentTabKey]);

  const handleChange = (key:string) => {
    searchDs.reset();
    listDs.setQueryParameter('type', key);
    listDs.setQueryParameter('params', '');
    listDs.setQueryParameter('status', '');
    mainStore.setCurrentTabKey(key);
    listDs.query();
  };

  const handleSearch = () => {
    const { params, status }:any = searchDs.toData()[0];
    listDs.setQueryParameter('type', mainStore.getCurrentTabKey);
    listDs.setQueryParameter('params', params);
    listDs.setQueryParameter('status', status);
    listDs.query();
  };

  return (
    <div className={`${prefixCls}-content-search`}>
      {HAS_BASE_PRO && <HostPick onChange={handleChange} hostTabKeys={hostTabKeys} />}
      <div style={{
        display: 'flex',
        alignItems: 'center',
      }}
      >
        <Form
          dataSet={searchDs}
          columns={7}
          className={`${prefixCls}-content-search-form`}
          labelLayout={'horizontal' as LabelLayoutType}
          labelWidth={1}
        >
          <TextField
            clearButton
            name="params"
            colSpan={4}
            placeholder="请输入搜索条件"
            prefix={<Icon type="search" style={{ color: '#CACAE4', lineHeight: '22px' }} />}
            onClear={handleSearch}
          />
          <Select
            label="主机状态:"
            name="status"
            colSpan={3}
            placeholder="请选择"
            onClear={handleSearch}
            options={statusDs}
          />
        </Form>
        <Button
          funcType={'flat' as FuncType}
          color={'primary' as ButtonColor}
          onClick={handleSearch}
          className={`${prefixCls}-content-search-btn`}
          disabled={listDs.status === 'loading'}
        >
          {formatMessage({ id: 'search' })}
        </Button>
      </div>
    </div>
  );
});

export default ContentHeader;
