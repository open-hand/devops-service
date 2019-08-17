import React, { useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import TimePopover from '../../../../../../components/timePopover';
import SyncSituation from './SyncSituation';
import { useResourceStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';

import './index.less';

export default function Situation() {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const { gitopsLogDs } = useEnvironmentStore();

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
      width: 80,
      sortable: true,
      renderer: ({ value }) => <TimePopover content={value} />,
    },
  ]), []);

  return (
    <div className={`${prefixCls}-environment-sync`}>
      <SyncSituation />
      <div className={`${prefixCls}-environment-sync-table-title`}>
        <FormattedMessage id={`${intlPrefix}.environment.error.logs`} />
      </div>
      <Table
        dataSet={gitopsLogDs}
        border={false}
        queryBar="none"
        columns={columns}
      />
    </div>
  );
}
