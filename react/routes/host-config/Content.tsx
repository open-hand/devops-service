import React from 'react';
import { observer } from 'mobx-react-lite';
import {
  Page, Header, Breadcrumb, Content, Permission,
} from '@choerodon/boot';
import {
  Button, Modal, Select, TextField,
} from 'choerodon-ui/pro';
import map from 'lodash/map';
import countBy from 'lodash/countBy';
import { ButtonColor, FuncType } from 'choerodon-ui/pro/lib/button/enum';
import { LabelLayout } from 'choerodon-ui/pro/lib/form/enum';
import ContentHeader from '@/routes/host-config/components/content-header';
import ContentList from '@/routes/host-config/components/content-list';
import CreateHost from '@/routes/host-config/components/create-host';
import { useHostConfigStore } from './stores';

const HostConfig: React.FC<any> = observer((): any => {
  const {
    prefixCls,
    intlPrefix,
    formatMessage,
  } = useHostConfigStore();

  const handleAdjustment = () => {

  };

  const handleAdd = () => {
    Modal.open({
      key: Modal.key(),
      title: formatMessage({ id: `${intlPrefix}.add` }),
      style: {
        width: 380,
      },
      drawer: true,
      children: <CreateHost />,
      okText: formatMessage({ id: 'create' }),
    });
  };

  return (
    <Page>
      <Header>
        <Permission service={[]}>
          <Button
            color={'primary' as ButtonColor}
            icon="add"
            onClick={handleAdd}
          >
            {formatMessage({ id: `${intlPrefix}.add` })}
          </Button>
          <Button
            color={'primary' as ButtonColor}
            icon="refresh"
            onClick={handleAdjustment}
          >
            {formatMessage({ id: `${intlPrefix}.adjustment` })}
          </Button>
        </Permission>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-content`}>
        <ContentHeader />
        <ContentList />
      </Content>
    </Page>
  );
});

export default HostConfig;
