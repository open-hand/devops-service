import React, { Fragment, useMemo, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import { Tooltip } from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import StatusIcon from '../../../../../components/StatusIcon';
import { useResourceStore } from '../../../stores';
import { useCertificateStore } from './stores';
import Modals from './modals';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import { getTimeLeft } from '../../../../../utils';
import { useMainStore } from '../../stores';
import ResourceListTitle from '../../components/resource-list-title';

import './index.less';

const { Column } = Table;

const CertContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    treeDs,
    resourceStore: { getSelectedMenu: { parentId } },
  } = useResourceStore();
  const {
    certificateDs,
    intl: { formatMessage },
    AppState: { currentMenuType: { projectId } },
  } = useCertificateStore();
  const {
    mainStore: { openDeleteModal },
  } = useMainStore();

  function refresh() {
    treeDs.query();
    certificateDs.query();
  }

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    const error = record.get('error');
    return (
      <div className="c7n-network-service">
        <StatusIcon
          name={value}
          status={commandStatus || ''}
          error={error || ''}
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
      <Tooltip
        title={content}
        trigger="hover"
        placement="top"
      >
        <span>{msg}</span>
      </Tooltip>
    );
  }

  function renderAction({ record }) {
    const commandStatus = record.get('commandStatus');
    const id = record.get('id');
    const name = record.get('certName');
    if (commandStatus === 'operating') {
      return null;
    }
    const buttons = [
      {
        service: ['devops-service.certification.delete'],
        text: formatMessage({ id: 'delete' }),
        action: () => openDeleteModal(parentId, id, name, 'certificate', refresh),
      },
    ];

    return (<Action data={buttons} />);
  }

  return (
    <div className={`${prefixCls}-ingress-table`}>
      <Modals />
      <ResourceListTitle type="certifications" />
      <Table
        dataSet={certificateDs}
        border={false}
        queryBar="bar"
      >
        <Column name="certName" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="domains" renderer={renderDomains} />
        <Column renderer={renderValid} header={formatMessage({ id: 'validDate' })} width="1rem" />
      </Table>
    </div>
  );
});

export default CertContent;
