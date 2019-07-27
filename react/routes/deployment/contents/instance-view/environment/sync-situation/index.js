import React, { Fragment, useContext, useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Permission } from '@choerodon/boot';
import {
  Table,
  DataSet,
} from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import TimePopover from '../../../../../../components/timePopover';
import TableDataSet from './stores/TableDataSet';
import Store from '../../../../stores';
// import SyncSituation from './SyncSituation';

import './index.less';

export default function EnvLog() {
  const {
    prefixCls,
    intlPrefix,
    intl,
    AppState: {
      currentMenuType: {
        id,
      },
    },
    selectedMenu: { menuId },
  } = useContext(Store);
  const tableDs = useMemo(() => new DataSet(TableDataSet({
    intl,
    intlPrefix,
    projectId: id,
    envId: menuId,
  })), [id, intl, intlPrefix, menuId]);

  const columns = useMemo(() => ([
    {
      name: 'error',
      renderer: ({ value }) => (
        <MouserOverWrapper text={value || ''} width={0.5}>
          {value}
        </MouserOverWrapper>
      ),
    },
    {
      name: 'filePath',
      renderer: ({ record }) => (
        <a
          href={record.data.fileUrl}
          target="_blank"
          rel="nofollow me noopener noreferrer"
        >
          <span>{record.data.filePath}</span>
        </a>
      ),
    },
    {
      name: 'commit',
      renderer: ({ record }) => (
        <a
          href={record.data.commitUrl}
          target="_blank"
          rel="nofollow me noopener noreferrer"
        >
          <span>{record.data.commit}</span>
        </a>
      ),
    },
    {
      name: 'errorTime',
      sortable: true,
      renderer: ({ value }) => <TimePopover content={value} />,
    },
  ]), []);

  function refresh() {
    tableDs.query();
  }

  return (
    <div className={`${prefixCls}-environment-sync`}>
      {/* <SyncSituation loadData={refresh} /> */}
      <div className={`${prefixCls}-environment-sync-table-title`}>
        <FormattedMessage id={`${intlPrefix}.environment.error.logs`} />
      </div>
      <Table
        dataSet={tableDs}
        border={false}
        queryBar="none"
        columns={columns}
      />
    </div>
  );
}
