import React, { useMemo, useContext } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import {
  Popover,
} from 'choerodon-ui';
import { Table, DataSet } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../../../components/StatusTags';
import Store from '../../../../../stores';
import TableDataSet from './stores/TableDataSet';
import TimePopover from '../../../../../../../components/timePopover/TimePopover';

const { Column } = Table;

const ConfigMap = observer(({ type }) => {
  const {
    selectedMenu: { menuId, parentId },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const tableDs = useMemo(() => new DataSet(TableDataSet({
    formatMessage,
    intlPrefix,
    type,
    projectId: id,
    envId: parentId,
    appId: menuId,
  })), [formatMessage, id, intlPrefix, menuId, parentId, type]);
  
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

  function renderAction({ record }) {
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        // action: () => handleEdit(record),
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        // action: () => handleDelete(record),
      },
    ];
    return <Action data={buttons} />;
  }

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

export default ConfigMap;
