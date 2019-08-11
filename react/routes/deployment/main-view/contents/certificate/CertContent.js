import React, { Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import {
  Popover,
} from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import StatusIcon from '../../../../../components/StatusIcon';
import { useDeploymentStore } from '../../../stores';
import { useCertificateStore } from './stores';
import Modals from './modals';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import { getTimeLeft } from '../../../../../utils';

import './index.less';

const { Column } = Table;

const CertContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { parentId } },
  } = useDeploymentStore();
  const {
    certificateDs,
    intl: { formatMessage },
  } = useCertificateStore();

  function refresh() {
    certificateDs.query();
  }

  function renderName({ value, record }) {
    return (
      <div className="c7n-network-service">
        <StatusIcon
          name={value}
          status={record.get('commandStatus') || ''}
          error={record.get('error') || ''}
        />
      </div>
    );
  }

  function renderDomains({ value }) {
    return (
      <MouserOverWrapper text={value[0] || ''} width={0.25}>
        {value[0]}
      </MouserOverWrapper>
    );
  }


  function renderValid({ record }) {
    const validFrom = record.get('validFrom');
    const validUntil = record.get('validUntil');
    const commandStatus = record.get('commandStatus');
    let msg = null;
    let content = null;
    if (!(validFrom && validUntil && commandStatus === 'success')) return content;

    content = (
      <Fragment>
        <FormattedMessage id="timeFrom" />：{validFrom}
        <br />
        <FormattedMessage id="timeUntil" />：{validUntil}
      </Fragment>
    );
    const start = new Date(validFrom.replace(/-/g, '/')).getTime();
    const end = new Date(validUntil.replace(/-/g, '/')).getTime();
    const now = Date.now();

    if (now < start) {
      msg = <FormattedMessage id="notActive" />;
    } else if (now > end) {
      msg = <FormattedMessage id="expired" />;
    } else {
      msg = getTimeLeft(now, end);
    }
    return (
      <Popover
        content={content}
        getPopupContainer={triggerNode => triggerNode.parentNode}
        trigger="hover"
        placement="top"
      >
        <span>{msg}</span>
      </Popover>
    );
  }

  function renderAction() {
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return (<Action data={buttons} />);
  }

  function handleDelete() {
    certificateDs.delete(certificateDs.current);
  }

  return (
    <div className={`${prefixCls}-ingress-table`}>
      <Modals />
      <Table
        dataSet={certificateDs}
        border={false}
        queryBar="none"
      >
        <Column name="certName" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="domains" renderer={renderDomains} />
        <Column renderer={renderValid} header={formatMessage({ id: 'validDate' })} />
      </Table>
    </div>
  );
});

export default CertContent;
