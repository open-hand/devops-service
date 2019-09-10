import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react';
import _ from 'lodash';
import TimeAgo from 'timeago-react';
import { Tooltip, Button, Icon, Popover, Spin } from 'choerodon-ui';
import { formatDate } from '../../../../../../utils';
import Store from '../stores';
import Pods from './pods';
import DetailsSidebar from './sidebar';

import './index.less';

function computedPodCount(collection) {
  // 计算 pod 数量和环形图占比
  const count = _.countBy(collection, (pod) => !!pod.ready);
  // 通过 ready 字段进行分类计数，分类结果是 true 和 false 的数量
  const correctCount = count.true || 0;
  const errorCount = count.false || 0;
  const sum = correctCount + errorCount;
  const correct = sum > 0 ? (correctCount / sum) * (Math.PI * 2 * 30) : 0;
  return {
    sum,
    correct,
    correctCount,
  };
}

const Label = ({ name, value }) => (<div className="c7ncd-deployment-popover-labels">
  <span>键-{name}</span>
  <span>值-{value}</span>
</div>);

Label.propTypes = {
  name: PropTypes.string.isRequired,
  value: PropTypes.string,
};

@observer
export default class Details extends Component {
  static contextType = Store;

  state = {
    visible: false,
    isDisabled: false,
  };

  componentWillUnmount() {
    const { detailsStore } = this.context;
    detailsStore.setResources({});
  }

  refresh = () => {
    const {
      baseDs,
      treeDs,
      detailsStore,
      instanceId,
      AppState: {
        currentMenuType: {
          projectId,
        },
      },
    } = this.context;
    baseDs.query();
    treeDs.query();
    detailsStore.loadResource(projectId, instanceId);
  };

  changeTargetCount = (count) => {
    const { detailsStore } = this.context;
    detailsStore.setTargetCount(count);
  };

  /**
   * 打开Deployment详情侧边栏，并加载数据
   * @param type
   * @param {*} id
   * @param {*} name
   */
  handleClick = async (type, id, name) => {
    this.setState({ isDisabled: true });
    const {
      detailsStore,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.context;
    const result = await detailsStore.loadDeploymentsJson(type, projectId, id, name);
    if (result) {
      this.setState({ visible: true });
    }
    this.setState({ isDisabled: false });
  };

  getDeployContent = (podType) => {
    const {
      detailsStore,
      baseDs,
      instanceId,
      envId,
    } = this.context;
    let status;
    let connect;
    const record = baseDs.current;
    if (record) {
      status = record.get('status');
      connect = record.get('connect');
    }
    const { isDisabled } = this.state;
    const {
      getResources,
      getTargetCount: targetCount,
    } = detailsStore;
    const POD_TYPE = {
      // 确保“当前/需要/可提供”的顺序
      deploymentVOS: ['current', 'desired', 'available'],
      daemonSetVOS: ['currentScheduled', 'desiredScheduled', 'numberAvailable'],
      statefulSetVOS: ['currentReplicas', 'desiredReplicas', 'readyReplicas'],
    };
    const [current, desired, available] = POD_TYPE[podType];
    const deployments = getResources[podType];

    if (!deployments || !deployments.length) return null;

    return _.map(deployments, (item) => {
      const { name, age, devopsEnvPodVOS, ports, labels } = item;
      const replica = `${item[available] || 0} available / ${item[current]
      || 0} current / ${item[desired] || 0} desired`;
      const podCount = computedPodCount(devopsEnvPodVOS);
      let portValues = null;
      let labelValues = null;
      if (ports && ports.length) {
        portValues = <Fragment>
          <span className="c7n-deploy-expanded-values">
            {_.head(ports)}
          </span>
          {ports.length > 1 && <Popover
            arrowPointAtCenter
            placement="bottom"
            getPopupContainer={(triggerNode) => triggerNode.parentNode}
            content={_.map(_.tail(ports), (port) => <div className="c7ncd-deployment-popover-port" key={port}>{port}</div>)}
          >
            <Icon type="expand_more" className="c7ncd-deployment-icon-more" />
          </Popover>}
        </Fragment>;
      }

      if (!_.isEmpty(labels)) {
        const keys = Object.keys(labels);
        const firstKey = keys[0];

        labelValues = <Fragment>
          <span className="c7n-deploy-expanded-values">
            <Label name={firstKey} value={labels[firstKey]} />
          </span>
          {keys.length > 1 && <Popover
            arrowPointAtCenter
            placement="bottom"
            getPopupContainer={(triggerNode) => triggerNode.parentNode}
            content={_.map(_.tail(keys), (key) => <Label
              key={key}
              name={key}
              value={labels[key]}
            />)}
          >
            <Icon type="expand_more" className="c7ncd-deployment-icon-more" />
          </Popover>}
        </Fragment>;
      }

      return (
        <div key={name} className="c7n-deploy-expanded-item">
          <ul className="c7n-deploy-expanded-text">
            <li className="c7n-deploy-expanded-lists">
              <span className="c7n-deploy-expanded-keys c7n-expanded-text_bold">
                <FormattedMessage id="ist.expand.name" />：
              </span>
              <span
                title={name}
                className="c7n-deploy-expanded-values c7n-expanded-text_bold"
              >
                {name}
              </span>
            </li>
            <li className="c7n-deploy-expanded-lists">
              <span className="c7n-deploy-expanded-keys">
                {podType === 'deploymentVOS' ? 'ReplicaSet' : <FormattedMessage id="status" />}
                ：
              </span>
              <span title={replica} className="c7n-deploy-expanded-values">{replica}</span>
            </li>
            <li className="c7n-deploy-expanded-lists">
              <span className="c7n-deploy-expanded-keys">
                <FormattedMessage id="ist.expand.date" />：
              </span>
              <span className="c7n-deploy-expanded-values">
                <Tooltip title={formatDate(age)}>
                  <TimeAgo
                    datetime={age}
                    locale={Choerodon.getMessage('zh_CN', 'en')}
                  />
                </Tooltip>
              </span>
            </li>
            {_.has(item, 'ports') && <li className="c7n-deploy-expanded-lists">
              <span className="c7n-deploy-expanded-keys">
                <FormattedMessage id="c7ncd.deployment.port.number" />：
              </span>
              {portValues}
            </li>}
            {_.has(item, 'labels') && <li className="c7n-deploy-expanded-lists">
              <span className="c7n-deploy-expanded-keys">
                <FormattedMessage id="label" />：
              </span>
              {labelValues}
            </li>}
            <li className="c7n-deploy-expanded-lists">
              <Button
                className="c7ncd-detail-btn"
                onClick={isDisabled ? null : () => this.handleClick(podType, instanceId, name)}
              >
                <FormattedMessage id="detailMore" />
              </Button>
            </li>
          </ul>
          <div className="c7n-deploy-expanded-pod">
            <Pods
              podType={podType}
              connect={connect}
              name={name}
              count={podCount}
              targetCount={targetCount}
              status={status}
              handleChangeCount={this.changeTargetCount}
              store={detailsStore}
              envId={envId}
              refresh={this.refresh}
            />
          </div>
        </div>
      );
    });
  };

  /**
   * PVC service ingress 三个没有pod圈
   * @param type
   */
  getNoPodContent = (type) => {
    const {
      detailsStore: {
        getResources,
      },
    } = this.context;
    const resources = getResources[type];

    if (!resources || !resources.length) return null;
    const TYPE_KEY = {
      serviceVOS: {
        leftItems: ['type', 'externalIp', 'age'],
        rightItems: ['clusterIp', 'port'],
      },
      ingressVOS: {
        leftItems: ['type', 'ports'],
        rightItems: ['address', 'age'],
      },
      persistentVolumeClaimVOS: {
        leftItems: ['status', 'accessModes'],
        rightItems: ['capacity', 'age'],
      },
    };

    return _.map(resources, (data) => {
      const content = (key) => {
        let text = null;
        switch (key) {
          case 'age':
            text = (
              <Tooltip title={formatDate(data[key])}>
                <TimeAgo
                  datetime={data[key]}
                  locale={Choerodon.getMessage('zh_CN', 'en')}
                />
              </Tooltip>
            );
            break;
          case 'status':
            text = (
              <span className={`c7n-deploy-pvc c7n-deploy-pvc_${data[key]}`}>
                {data[key]}
              </span>
            );
            break;
          default:
            text = data[key];
            break;
        }
        return (
          <li className="c7n-deploy-expanded-lists">
            <span className="c7n-deploy-expanded-keys">
              <FormattedMessage id={`ist.expand.net.${key}`} />：
            </span>
            <span className="c7n-deploy-expanded-values">{text}</span>
          </li>
        );
      };
      return (
        <Fragment key={data.name}>
          <div className="c7n-deploy-expanded-lists">
            <span className="c7n-deploy-expanded-keys c7n-expanded-text_bold">
              <FormattedMessage id="ist.expand.name" />：
            </span>
            <span
              title={data.name}
              className="c7n-deploy-expanded-values c7n-expanded-text_bold"
            >
              {data.name}
            </span>
          </div>
          <div className="c7n-deploy-expanded-item">
            <ul className="c7n-deploy-expanded-text">
              {_.map(TYPE_KEY[type].leftItems, (item) => content(item))}
            </ul>
            <ul className="c7n-deploy-expanded-text">
              {_.map(TYPE_KEY[type].rightItems, (item) => content(item))}
            </ul>
          </div>
        </Fragment>
      );
    });
  };

  hideSidebar = () => {
    const { detailsStore } = this.context;
    this.setState({ visible: false });
    detailsStore.setDeployments([]);
  };

  render() {
    const {
      detailsStore: {
        getLoading,
      },
    } = this.context;
    const { visible } = this.state;

    const contentList = [
      {
        title: 'Deployments',
        main: this.getDeployContent('deploymentVOS'),
      },
      {
        title: 'Stateful Set',
        main: this.getDeployContent('statefulSetVOS'),
      },
      {
        title: 'Daemon Set',
        main: this.getDeployContent('daemonSetVOS'),
      },
      {
        title: 'PVC',
        main: this.getNoPodContent('persistentVolumeClaimVOS'),
      },
      {
        title: 'Service',
        main: this.getNoPodContent('serviceVOS'),
      },
      {
        title: 'Ingress',
        main: this.getNoPodContent('ingressVOS'),
      },
    ];

    const hasContent = _.find(contentList, (item) => item.main && item.main.length);

    return (
      <Fragment>
        <Spin spinning={getLoading}>
          <div className="c7n-deploy-expanded">
            {_.map(contentList, ({ main, title }) => (main && main.length ? <Fragment key={title}>
              <div className="c7n-deploy-expanded-title">
                <span>{title}</span>
              </div>
              {main}
            </Fragment> : null))}
            {!hasContent ? (
              <div className="c7n-deploy-expanded-empty">
                <FormattedMessage id="ist.expand.empty" />
              </div>
            ) : null}
          </div>
        </Spin>
        {visible && <DetailsSidebar
          visible={visible}
          onClose={this.hideSidebar}
        />}
      </Fragment>
    );
  }
}
