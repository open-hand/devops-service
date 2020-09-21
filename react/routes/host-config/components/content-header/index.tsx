import React from 'react';
import { observer } from 'mobx-react-lite';
import {
  Button, Form, Icon, Select, TextField,
} from 'choerodon-ui/pro';
import {
  ButtonColor, FuncType, LabelLayoutType,
} from '../../../../interface';

import { useHostConfigStore } from '../../stores';

import HostPick from '../host-pick';

const ContentHeader: React.FC<any> = observer((): any => {
  const {
    prefixCls,
    intlPrefix,
    formatMessage,
    searchDs,
    hostTabKeys,
    listDs,
    mainStore,
  } = useHostConfigStore();

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
      <HostPick onChange={handleChange} hostTabKeys={hostTabKeys} />
      <div style={{
        display: 'flex',
        alignItems: 'center',
      }}
      >
        <Form
          dataSet={searchDs}
          columns={6}
          className={`${prefixCls}-content-search-form`}
          labelLayout={'horizontal' as LabelLayoutType}
        >
          <TextField
            clearButton
            name="params"
            colSpan={3}
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
