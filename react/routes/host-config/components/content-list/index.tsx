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
import EmptyPage from '@/components/empty-page';
import { useHostConfigStore } from '../../stores';
import HostPick from '../host-pick';

const ContentList: React.FC<any> = observer((): any => {
  const {
    prefixCls,
    intlPrefix,
    formatMessage,
    listDs,
  } = useHostConfigStore();

  const handleChange = () => {
  };

  const handleSearch = () => {

  };

  if (listDs && listDs.status !== 'loading' && !listDs.length) {
    return <EmptyPage />;
  }

  return (
    <div className={`${prefixCls}-content-list`}>
      {listDs.map((record) => (
        <div key={record.id} className={`${prefixCls}-content-list-item`}>
          主机
        </div>
      ))}
    </div>
  );
});

export default ContentList;
