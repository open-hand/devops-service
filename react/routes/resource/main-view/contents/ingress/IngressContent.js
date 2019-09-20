import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import map from 'lodash/map';
import StatusIcon from '../../../../../components/StatusIcon';
import StatusTags from '../../../../../components/status-tag';

import { useResourceStore } from '../../../stores';
import { useIngressStore } from './stores';
import Modals from './modals';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import DomainModal from '../application/modals/domain';
import { useMainStore } from '../../stores';

import './index.less';


const serviceStyle = {
  minWidth: 40,
  marginRight: 8,
  height: '.16rem',
  lineHeight: '.16rem',
};


const { Column } = Table;

const IngressContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
    treeDs,
  } = useResourceStore();
  const {
    ingressDs,
    intl: { formatMessage },
  } = useIngressStore();
  const { ingressStore } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    treeDs.query();
    ingressDs.query();
  }

  function renderName({ record }) {
    const name = record.get('name');
    const status = record.get('status');
    const error = record.get('error');
    const commandStatus = record.get('commandStatus');
    return (
      <Fragment>
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={{ minWidth: 40, marginRight: '0.08rem', height: '0.16rem', lineHeight: '0.16rem' }}
        />
        <StatusIcon
          name={name}
          status={status || ''}
          error={error || ''}
        />
      </Fragment>
    );
  }

  function renderPath({ value }) {
    return (
      map(value, ({ path }) => (
        <div
          className="c7n-network-col_border"
          key={path}
        >
          <span>{path}</span>
        </div>
      ))
    );
  }

  function renderService({ record }) {
    return (
      map(record.get('pathList'), ({ serviceStatus, serviceName }) => (
        <div
          className="c7n-network-service"
          key={record.get('id')}
        >
          <StatusTags
            colorCode={serviceStatus}
            name={formatMessage({ id: serviceStatus })}
            style={serviceStyle}
          />
          <MouserOverWrapper text={serviceName} width={0.1}>
            {serviceName}
          </MouserOverWrapper>
        </div>
      ))
    );
  }

  function renderAction({ record }) {
    const commandStatus = record.get('commandStatus');
    if (commandStatus === 'operating') {
      return null;
    }
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: openModal,
      },
      {
        service: ['devops-service.devops-ingress.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return (<Action data={buttons} />);
  }

  function handleDelete() {
    ingressDs.delete(ingressDs.current);
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }

  return (
    <div className={`${prefixCls}-ingress-table`}>
      <Modals />
      <Table
        dataSet={ingressDs}
        border={false}
        queryBar="bar"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="domain" />
        <Column name="pathList" renderer={renderPath} />
        <Column name="pathList" renderer={renderService} header={formatMessage({ id: 'network' })} />
      </Table>
      {showModal && (
        <DomainModal
          envId={parentId}
          id={ingressDs.current.get('id')}
          visible={showModal}
          type="edit"
          store={ingressStore}
          onClose={closeModal}
        />
      )}
    </div>
  );
});

export default IngressContent;
