import React from 'react';
import { TabPage, Content, Breadcrumb } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import TimePopover from '../../../components/timePopover/TimePopover';

import './index.less';

const { Column } = Table;

const Version = () => {
  const { prefixCls } = useAppTopStore();
  const {
    detailDs,
    versionDs,
  } = useServiceDetailStore();

  function refresh() {
    versionDs.query();
  }

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function getTitle() {
    if (detailDs.current) {
      return detailDs.current.get('name');
    }
  }

  return (
    <TabPage
      service={[
        'devops-service.app-service.query',
        'devops-service.app-service.update',
        'devops-service.app-service.updateActive',
        'devops-service.app-service-version.pageByOptions',
      ]}
    >
      <HeaderButtons>
        <Button
          icon="refresh"
          onClick={refresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </HeaderButtons>
      <Breadcrumb title={getTitle()} />
      <Content className={`${prefixCls}-detail-content`}>
        <Table dataSet={versionDs}>
          <Column name="version" sortable />
          <Column name="creationDate" renderer={renderTime} />
        </Table>
      </Content>
    </TabPage>
  );
};

export default Version;
