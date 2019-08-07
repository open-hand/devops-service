import React, { Component, Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react';
import _ from 'lodash';
import TimeAgo from 'timeago-react';
import { Tooltip, Button } from 'choerodon-ui';
import { formatDate } from '../../../../../../utils';
import Store from './stores';
import Pods from './pods';
import DetailsSidebar from './sidebar';

import './index.less';

function computedPodCount(collection) {
  // 计算 pod 数量和环形图占比
  const count = _.countBy(collection, pod => !!pod.ready);
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


@observer
export default class Details extends Component {
  static contextType = Store;

  constructor(props) {
    super(props);
    this.state = {
      visible: false,
    };
  }

  changeTargetCount = (count) => {
    const { detailsStore } = this.context;
    detailsStore.setTargetCount(count);
  };

  /**
   * Deployments 渲染
   * @param podType
   * @param item
   * @param id
   * @returns {*}
   */
  getDeployContent = (podType, item, id) => {
    const { detailsStore } = this.context;
    const { name, age, devopsEnvPodDTOS } = item;
    const POD_TYPE = {
      // 确保“当前/需要/可提供”的顺序
      deploymentDTOS: ['current', 'desired', 'available'],
      daemonSetDTOS: ['currentScheduled', 'desiredScheduled', 'numberAvailable'],
      statefulSetDTOS: ['currentReplicas', 'desiredReplicas', 'readyReplicas'],
    };
    const [current, desired, available] = POD_TYPE[podType];
    const replica = `${item[available] || 0} available / ${item[current]
    || 0} current / ${item[desired] || 0} desired`;

    const podCount = computedPodCount(devopsEnvPodDTOS);
    const targetCount = detailsStore.getTargetCount;

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
              {podType === 'deploymentDTOS' ? 'ReplicaSet' : <FormattedMessage id="status" />}
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
          <li className="c7n-deploy-expanded-lists">
            <Button
              className="c7ncd-detail-btn"
              onClick={() => this.handleClick(podType, id, name)}
            >
              <FormattedMessage id="detailMore" />
            </Button>
          </li>
        </ul>
        <div className="c7n-deploy-expanded-pod">
          <Pods
            podType={podType}
            // connect={connect}
            name={name}
            count={podCount}
            targetCount={targetCount}
            // status={status}
            handleChangeCount={this.changeTargetCount}
            store={detailsStore}
          />
        </div>
      </div>
    );
  };

  /**
   * PVC service ingress 三个没有pod圈
   * @param {string} type
   * @param {array} data
   */
  getNoPodContent = (type, data) => {
    const TYPE_KEY = {
      serviceDTOS: {
        leftItems: ['type', 'externalIp', 'age'],
        rightItems: ['clusterIp', 'port'],
      },
      ingressDTOS: {
        leftItems: ['type', 'ports'],
        rightItems: ['address', 'age'],
      },
      persistentVolumeClaimDTOS: {
        leftItems: ['status', 'accessModes'],
        rightItems: ['capacity', 'age'],
      },
    };
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
      <div key={data.name} className="c7n-deploy-expanded-item">
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
        <ul className="c7n-deploy-expanded-text c7n-deploy-expanded_half">
          {_.map(TYPE_KEY[type].leftItems, item => content(item))}
        </ul>
        <ul className="c7n-deploy-expanded-text c7n-deploy-expanded_half">
          {_.map(TYPE_KEY[type].rightItems, item => content(item))}
        </ul>
      </div>
    );
  };

  /**
   * 打开Deployment详情侧边栏，并加载数据
   * @param {string} type
   * @param {*} id
   * @param {*} name
   */
  handleClick = (type, id, name) => {
    const {
      detailsStore,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.context;
    this.setState({ visible: true });
    detailsStore.loadDeploymentsJson(type, projectId, id, name);
  };

  hideSidebar = () => {
    const { detailsStore } = this.context;
    this.setState({ visible: false });
    detailsStore.setDeployments([]);
  };


  render() {
    const {
      detailsStore: {
        getResources,
        menuId,
      },
    } = this.context;
    const { visible } = this.state;

    const getPodContent = dto => _.map(getResources[dto], item => this.getDeployContent(dto, item, menuId));

    const getNoPodContent = dto => _.map(getResources[dto], item => this.getNoPodContent(dto, item));

    const contentList = [
      {
        title: 'Deployments',
        main: getPodContent('deploymentDTOS'),
      },
      {
        title: 'Stateful Set',
        main: getPodContent('statefulSetDTOS'),
      },
      {
        title: 'Daemon Set',
        main: getPodContent('daemonSetDTOS'),
      },
      {
        title: 'PVC',
        main: getNoPodContent('persistentVolumeClaimDTOS'),
      },
      {
        title: 'Service',
        main: getNoPodContent('serviceDTOS'),
      },
      {
        title: 'Ingress',
        main: getNoPodContent('ingressDTOS'),
      },
    ];

    const hasContent = _.find(contentList, item => item.main.length);

    return (
      <Fragment>
        <div className="c7n-deploy-expanded">
          {_.map(contentList, (dto) => {
            const { main, title } = dto;
            return main.length ? (
              <Fragment key={title}>
                <div className="c7n-deploy-expanded-title">
                  <span>{title}</span>
                </div>
                {main}
              </Fragment>
            ) : null;
          })}
          {!hasContent ? (
            <div className="c7n-deploy-expanded-empty">
              <FormattedMessage id="ist.expand.empty" />
            </div>
          ) : null}
        </div>
        {visible && <DetailsSidebar
          visible={visible}
          onClose={this.hideSidebar}
        />}
      </Fragment>
    );
  }
}
