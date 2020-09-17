import React from 'react';
import { observer } from 'mobx-react-lite';
import {
  Page, Header, Breadcrumb, Content, Permission,
} from '@choerodon/boot';
import {
  Button, Form, Select, TextField,
} from 'choerodon-ui/pro';
import map from 'lodash/map';
import countBy from 'lodash/countBy';
import { ButtonColor, FuncType } from 'choerodon-ui/pro/lib/button/enum';
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
    listDs.setQueryParameter('type', key);
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
      <Form
        dataSet={searchDs}
        columns={6}
        className={`${prefixCls}-content-search-form`}
      >
        <TextField name="params" colSpan={3} placeholder="请输入搜索条件" />
        <Select name="status" colSpan={2} onClear={handleSearch} />
        <Button
          funcType={'flat' as FuncType}
          color={'primary' as ButtonColor}
          onClick={() => handleSearch()}
        >
          {formatMessage({ id: 'search' })}
        </Button>
      </Form>
    </div>
  );
});

export default ContentHeader;
