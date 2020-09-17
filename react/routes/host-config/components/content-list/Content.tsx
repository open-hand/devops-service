import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Page,
  Header,
  Breadcrumb,
  Content,
  Permission,
  Action,
} from '@choerodon/boot';
import {
  Button, Form, Pagination, Select, TextField,
} from 'choerodon-ui/pro';
import map from 'lodash/map';
import countBy from 'lodash/countBy';
import { ButtonColor, FuncType } from 'choerodon-ui/pro/lib/button/enum';
import EmptyPage from '@/components/empty-page';
import Loading from '@/components/loading';
import HostsItem from './components/hostItem';
import { useHostConfigStore } from '../../stores';

const ContentList: React.FC<any> = observer((): any => {
  const {
    prefixCls, intlPrefix, formatMessage, listDs,
  } = useHostConfigStore();

  useEffect(() => {
  }, []);

  if (listDs.status === 'loading' || !listDs) {
    return <Loading display />;
  }

  if (listDs && !listDs.length) {
    return <EmptyPage />;
  }

  return (
    <>
      <div className={`${prefixCls}-content-list`}>
        {listDs.map((record) => (
          <HostsItem
            {...record.data}
            record={record}
            listDs={listDs}
          />
        ))}
      </div>
      <Pagination
        className={`${prefixCls}-content-pagination`}
        dataSet={listDs}
      />
    </>
  );
});

export default ContentList;
