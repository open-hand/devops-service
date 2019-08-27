import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import TimePopover from '../../../../../../components/time-popover';
import SyncSituation from './Situation';
import { useEnvironmentStore } from '../../../../stores';
import { useDetailStore } from '../stores';

import './index.less';

const { Column } = Table;

export default function Situation() {
  const {
    prefixCls,
    intlPrefix,
    gitopsLogDs,
  } = useDetailStore();

  function renderMsg({ value }) {
    return <MouserOverWrapper text={value || ''} width={0.5}>
      {value}
    </MouserOverWrapper>;
  }

  function renderFileLink({ record }) {
    const url = record.get('fileUrl');
    const path = record.get('filePath');
    return <a
      href={url}
      target="_blank"
      rel="nofollow me noopener noreferrer"
    >
      <span>{path}</span>
    </a>;
  }

  function renderCommit({ record }) {
    const url = record.get('commitUrl');
    const commit = record.get('commit');
    return <a
      href={url}
      target="_blank"
      rel="nofollow me noopener noreferrer"
    >
      <span>{commit}</span>
    </a>;
  }

  function renderTime({ value }) {
    return <TimePopover datetime={value} />;
  }

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
      >
        <Column name="error" renderer={renderMsg} />
        <Column name="filePath" renderer={renderFileLink} />
        <Column name="commit" renderer={renderCommit} />
        <Column name="errorTime" sortable renderer={renderTime} />
      </Table>
    </div>
  );
}
