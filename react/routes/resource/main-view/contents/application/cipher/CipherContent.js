import React, { useMemo, useContext, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import {
  Popover,
} from 'choerodon-ui';
import { Table, DataSet } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../../components/StatusTags';
import { useResourceStore } from '../../../../stores';
import { useCipherStore } from './stores';
import TimePopover from '../../../../../../components/timePopover/TimePopover';

const { Column } = Table;

const Cipher = observer((props) => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    intl: { formatMessage },
    tableDs,
  } = useCipherStore();

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    return (
      <div>
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={{ minWidth: 40, marginRight: '0.08rem' }}
        />
        <span>{value}</span>
      </div>
    );
  }

  function renderKey({ value = [], record }) {
    return (
      <MouserOverWrapper width={0.5}>
        {value.join(',')}
      </MouserOverWrapper>
    );
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  const renderAction = useCallback(({ record }) => {
    const handleDelete = () => tableDs.delete(record);
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: () => handleDelete(record),
      },
    ];
    return <Action data={buttons} />;
  }, [formatMessage, tableDs]);

  return (
    <div className={`${prefixCls}-application-mapping`}>
      <Table
        dataSet={tableDs}
        border={false}
        queryBar="bar"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} />
        <Column name="key" renderer={renderKey} />
        <Column name="lastUpdateDate" renderer={renderDate} />
      </Table>
    </div>
  );
});

export default Cipher;
