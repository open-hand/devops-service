import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import {
  Tooltip,
  Icon,
  Popover,
} from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import _ from 'lodash';
import classnames from 'classnames';
import StatusIcon from '../../../../../components/StatusIcon';
import { useDeploymentStore } from '../../../stores';
import { useNetworkStore } from './stores';
import Modals from './modals';
import EditNetwork from './modals/network-edit';

import './index.less';

const { Column } = Table;

const NetworkContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { parentId } },
  } = useDeploymentStore();
  const {
    networkDs,
    networkStore,
    intl: { formatMessage },
  } = useNetworkStore();

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    networkDs.query();
  }

  function renderName({ record }) {
    const name = record.get('name');
    const status = record.get('status');
    const error = record.get('error');

    return (
      <StatusIcon
        name={name}
        status={status || ''}
        error={error || ''}
      />
    );
  }

  function renderTargetType({ record }) {
    const { instances, labels } = record.get('target') || {};
    const appId = record.get('appServiceId');

    let type = 'EndPoints';
    if (appId && instances && instances.length) {
      type = formatMessage({ id: 'instance' });
    } else if (labels) {
      type = formatMessage({ id: 'label' });
    }

    return <span>{type}</span>;
  }

  function renderTarget({ record }) {
    const { instances, labels, endPoints } = record.get('target') || {};
    const node = [];
    const port = [];
    const len = endPoints ? 2 : 1;
    if (instances && instances.length) {
      _.forEach(instances, ({ id: itemId, code, instanceStatus }) => {
        const targetClass = classnames({
          'net-target-item': true,
          'net-target-item-failed': instanceStatus !== 'operating' && instanceStatus !== 'running',
        });
        if (code) {
          node.push(
            <div className={targetClass} key={itemId}>
              <Tooltip
                title={formatMessage({ id: instanceStatus || `${intlPrefix}.application.net.deleted` })}
                placement="top"
              >
                {code}
              </Tooltip>
            </div>,
          );
        }
      });
    }
    if (!_.isEmpty(labels)) {
      _.forEach(labels, (value, key) => node.push(
        <div className="net-target-item" key={key}>
          <span>{key}</span>=<span>{value}</span>
        </div>,
      ),);
    }
    if (endPoints) {
      const targetIps = _.split(_.keys(endPoints)[0], ',');
      const portList = _.values(endPoints)[0];
      _.map(targetIps, (item, index) => node.push(
        <div className="net-target-item" key={index}>
          <span>{item}</span>
        </div>,
      ),);
      _.map(portList, (item, index) => {
        port.push(
          <div className="net-target-item" key={index}>
            <span>{item.port}</span>
          </div>,
        );
      });
    }
    return (
      <Fragment>
        {
          _.map([node, port], (item, index) => {
            if (item.length) {
              return (
                <div className="net-target-wrap" key={index}>
                  {item[0]}
                  {endPoints && (<div className="net-target-Ip">{item[1] || null}</div>)}
                  {item.length > len && (
                    <Popover
                      arrowPointAtCenter
                      placement="bottomRight"
                      getPopupContainer={triggerNode => triggerNode.parentNode}
                      content={<Fragment>{item}</Fragment>}
                    >
                      <Icon type="expand_more" className="net-expend-icon" />
                    </Popover>
                  )}
                </div>
              );
            }
          })
        }
      </Fragment>
    );
  }

  function renderConfigType({ record }) {
    const { externalIps, ports } = record.get('config') || {};
    const loadBalanceIp = record.get('loadBalanceIp');
    const type = record.get('type');
    const iPArr = [];
    const portArr = [];
    if (externalIps && externalIps.length) {
      _.forEach(externalIps, item => (
        iPArr.push(
          <div key={item} className="net-config-item">
            {item}
          </div>,
        )
      ));
    }
    if (ports && ports.length) {
      _.forEach(ports, ({ nodePort, port, targetPort }) => {
        portArr.push(
          <div key={port} className="net-config-item">
            {nodePort || (type !== 'ClusterIP' && formatMessage({ id: 'null' }))} {port} {targetPort}
          </div>,
        );
      });
    }

    let content = null;
    switch (type) {
      case 'ClusterIP':
        content = (
          <Fragment>
            <div className="net-config-wrap">
              <div className="net-type-title">
                <FormattedMessage id={`${intlPrefix}.application.net.ip`} />
              </div>
              <div>{externalIps ? iPArr : '-'}</div>
            </div>
            <div className="net-config-wrap">
              <div className="net-type-title">
                <FormattedMessage id={`${intlPrefix}.application.net.port`} />
              </div>
              <div>{portArr}</div>
            </div>
          </Fragment>
        );
        break;
      case 'NodePort':
        content = (
          <Fragment>
            <div className="net-config-item">
              <FormattedMessage id={`${intlPrefix}.application.net.nport`} />
            </div>
            <div>{portArr}</div>
          </Fragment>
        );
        break;
      case 'LoadBalancer':
        content = (
          <Fragment>
            <div className="net-config-wrap">
              <div className="net-type-title">
                <FormattedMessage id={`${intlPrefix}.application.net.nport`} />
              </div>
              <div>{portArr}</div>
            </div>
            {loadBalanceIp && (
              <div className="net-config-wrap">
                <div className="net-type-title">
                  <span>LoadBalancer IP</span>
                </div>
                <div>{loadBalanceIp}</div>
              </div>
            )}
          </Fragment>
        );
        break;
      default:
        break;
    }

    return (
      <div className="net-config-content">
        <span className="net-config-type">{type}</span>
        <Popover
          arrowPointAtCenter
          placement="bottomRight"
          getPopupContainer={triggerNode => triggerNode.parentNode}
          content={content}
        >
          <Icon type="expand_more" className="net-expend-icon" />
        </Popover>
      </div>
    );
  }

  function renderAction() {
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: openModal,
      },
      {
        service: ['devops-service.devops-service.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return (<Action data={buttons} />);
  }

  function handleDelete() {
    networkDs.delete(networkDs.current);
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }

  return (
    <div className={`${prefixCls}-network-table`}>
      <Modals />
      <Table
        dataSet={networkDs}
        border={false}
        queryBar="none"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column renderer={renderTargetType} header={formatMessage({ id: `${intlPrefix}.application.net.targetType` })} />
        <Column renderer={renderTarget} header={formatMessage({ id: `${intlPrefix}.application.net.target` })} />
        <Column name="type" renderer={renderConfigType} />
      </Table>
      {showModal && (
        <EditNetwork
          netId={networkDs.current.get('id')}
          envId={parentId}
          visible={showModal}
          store={networkStore}
          onClose={closeModal}
        />
      )}
    </div>
  );
});

export default NetworkContent;
