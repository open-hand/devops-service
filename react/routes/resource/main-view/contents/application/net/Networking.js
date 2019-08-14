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
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusIcon from '../../../../../../components/StatusIcon';
import { useDeploymentStore } from '../../../../stores';
import { useNetStore } from './stores';
import { useApplicationStore } from '../stores';
import DomainModal from '../modals/domain';
// import EditNetwork from '../../../../../network/network-edit';

import './index.less';

const { Column } = Table;

const Networking = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { menuId, parentId } },
  } = useDeploymentStore();
  const {
    tableDs,
    intl: { formatMessage },
  } = useNetStore();
  const {
    domainStore,
    networkStore,
  } = useApplicationStore();

  const [showDomain, setShowDomain] = useState(false);
  const [domainId, setDomainId] = useState(null);
  const [showNetwork, setNetwork] = useState(false);

  function refresh() {
    tableDs.query();
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
                      getPopupContainer={(triggerNode) => triggerNode.parentNode}
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
      _.forEach(externalIps, (item) => (
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
          getPopupContainer={(triggerNode) => triggerNode.parentNode}
          content={content}
        >
          <Icon type="expand_more" className="net-expend-icon" />
        </Popover>
      </div>
    );
  }

  function renderAction({ record }) {
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: () => showNetwork(true),
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        // action: () => handleDelete(record),
      },
    ];

    return (<Action data={buttons} />);
  }

  function openDomainEdit(itemId) {
    setDomainId(itemId);
    setShowDomain(true);
  }

  function closeDomainEdit(isLoad) {
    setDomainId(null);
    setShowDomain(false);
    isLoad && refresh();
  }

  function closeNetworkEdit(isLoad) {
    showNetwork(false);
    isLoad && refresh();
  }

  function renderExpandedRow({ record }) {
    const devopsIngressDTOS = record.get('devopsIngressDTOS');
    const button = {
      shape: 'circle',
      size: 'small',
      funcType: 'flat',
    };
    const content = devopsIngressDTOS && devopsIngressDTOS.length ? (
      _.map(devopsIngressDTOS, ({ id: itemId, name, domain, error, status, pathList }) => {
        const buttons = [
          {
            service: [],
            text: formatMessage({ id: 'edit' }),
            action: () => openDomainEdit(itemId),
          },
          {
            service: [],
            text: formatMessage({ id: 'delete' }),
            // action: () => handleIngressDelete(record),
          },
        ];
        return (
          <div key={itemId} className="net-expandedRow-detail">
            <FormattedMessage id={`${intlPrefix}.application.net.ingress`} />：
            <div className="net-ingress-text">
              <StatusIcon
                name={name}
                status={status}
                error={error}
              />
            </div>
            <Action data={buttons} />
            <FormattedMessage id="address" />：
            <div className="net-ingress-text">
              <MouserOverWrapper text={domain} width={0.2}>
                {domain}
              </MouserOverWrapper>
            </div>
            <FormattedMessage id="path" />：
            <div className="net-ingress-text">
              <MouserOverWrapper text={pathList[0] ? pathList[0].path : ''} width={0.2}>
                {pathList[0] ? pathList[0].path : ''}
              </MouserOverWrapper>
            </div>
          </div>
        );
      })
    ) : (<span className="net-no-ingress">{formatMessage({ id: `${intlPrefix}.application.net.empty` })}</span>);
    return (
      <div className="net-expandedRow-content">
        {content}
      </div>
    );
  }

  return (
    <div className={`${prefixCls}-application-net`}>
      <Table
        dataSet={tableDs}
        border={false}
        queryBar="none"
        expandedRowRenderer={renderExpandedRow}
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} />
        <Column renderer={renderTargetType} header={formatMessage({ id: `${intlPrefix}.application.net.targetType` })} />
        <Column renderer={renderTarget} header={formatMessage({ id: `${intlPrefix}.application.net.target` })} />
        <Column name="type" renderer={renderConfigType} />
      </Table>
      {showDomain && (
        <DomainModal
          envId={parentId}
          appServiceId={menuId}
          id={domainId}
          visible={showDomain}
          type="edit"
          store={domainStore}
          onClose={closeDomainEdit}
        />
      )}
      {/* showNetwork && (
        <EditNetwork
          netId={tableDs.current.get('id')}
          visible={showNetwork}
          store={networkStore}
          onClose={closeNetworkEdit}
        />
      ) */}
    </div>
  );
});

export default Networking;
