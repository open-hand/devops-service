import React, { PureComponent, Fragment } from 'react';
import { inject } from 'mobx-react';
import { FormattedMessage, injectIntl } from 'react-intl';
import debounce from 'lodash/debounce';
import assign from 'lodash/assign';
import { Button, Tooltip } from 'choerodon-ui';
import { Choerodon } from '@choerodon/boot';

import './index.less';

@inject('AppState')
@injectIntl
export default class Pods extends PureComponent {
  state = {
    btnDisable: false,
    textDisplay: false,
  };

  /**
   * 限制连续点击发送请求的次数
   * 限制600ms
   */
  operatePodCount = debounce((count) => {
    const {
      store,
      envId,
      name,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      refresh,
    } = this.props;
    store.operatePodCount(projectId, envId, name, count)
      .then(() => {
        refresh();
      })
      .catch((err) => {
        Choerodon.handleResponseError(err);
      });
  }, 600);

  /**
   * 环形图下的文字显示
   */
  changeTextDisplay = () => {
    let { textDisplay } = this.state;
    if (!textDisplay) {
      textDisplay = true;
    }
    this.setState({ textDisplay });
  };

  handleDecrease = () => {
    const {
      targetCount,
      handleChangeCount,
      podType,
      name,
      count: { sum },
    } = this.props;
    const currentPodTargetCount = targetCount[`${name}-${podType}`] || sum;
    let { btnDisable } = this.state;

    if (currentPodTargetCount > 1) {
      const count = currentPodTargetCount - 1;
      // 最小pod数为1
      if (count <= 1) {
        btnDisable = true;
      }
      this.changeTextDisplay();
      this.setState({ btnDisable });
      this.operatePodCount(count);
      handleChangeCount(
        assign({}, targetCount, { [`${name}-${podType}`]: count }),
      );
    }
  };

  handleIncrease = () => {
    const {
      targetCount,
      handleChangeCount,
      podType,
      name,
      count: { sum },
    } = this.props;
    const currentPodTargetCount = targetCount[`${name}-${podType}`] || sum;
    let { btnDisable } = this.state;

    const count = currentPodTargetCount + 1;

    this.changeTextDisplay();

    if (btnDisable && count > 1) {
      btnDisable = false;
    }

    this.setState({ btnDisable });
    this.operatePodCount(count);
    handleChangeCount(
      assign({}, targetCount, { [`${name}-${podType}`]: count }),
    );
  };

  /**
   * 获取 pod 的环形图
   * @readonly
   */
  get renderCircle() {
    const { count: { sum, correct, correctCount } } = this.props;
    return (
      <svg width="70" height="70">
        <circle
          cx="35"
          cy="35"
          r="30"
          strokeWidth={sum === 0 || sum > correctCount ? 5 : 0}
          stroke={sum > 0 ? '#ffb100' : '#f3f3f3'}
          className="c7n-pod-circle-error"
        />
        <circle
          cx="35"
          cy="35"
          r="30"
          className="c7n-pod-circle"
          strokeDasharray={`${correct}, 10000`}
        />
        <text x="50%" y="32.5" className="c7n-pod-circle-num">
          {sum}
        </text>
        <text x="50%" y="50" className="c7n-pod-circle-text">
          {sum > 1 ? 'pods' : 'pod'}
        </text>
      </svg>
    );
  }

  render() {
    const {
      podType,
      status,
      connect,
      name,
      count: { sum },
      targetCount,
      intl: { formatMessage },
      instanceStatus,
    } = this.props;
    const { btnDisable, textDisplay } = this.state;

    const currentPodTargetCount = targetCount[`${name}-${podType}`] || sum;

    // 实际pod数和目标数不同
    // 修改过pod数
    const show = textDisplay
      && sum !== currentPodTargetCount
      && connect
      && status === 'success';
    const descIsEnable = instanceStatus === 'stopped' || !connect
      || currentPodTargetCount <= 1
      || status !== 'success';

    return (
      <div className="c7ncd-pod">
        <div className="c7ncd-pod-wrap">
          <div className="c7ncd-pod-content">
            {this.renderCircle}
          </div>
          {podType === 'deploymentVOS' && (
            <div className="c7ncd-pod-content c7ncd-pod-btn-wrap">
              <Button
                disabled={!(connect && status === 'success' && instanceStatus !== 'stopped')}
                className="c7ncd-pod-btn"
                size="small"
                icon="expand_less"
                onClick={this.handleIncrease}
              />
              <Tooltip
                title={instanceStatus !== 'stopped' && connect && currentPodTargetCount === 1 && status === 'success' ? formatMessage({ id: 'c7ncd.deployment.pod.disabled.tops' }) : ''}
                placement="bottom"
              >
                <Button
                  disabled={descIsEnable}
                  className="c7ncd-pod-btn"
                  size="small"
                  icon="expand_more"
                  onClick={this.handleDecrease}
                  style={{ padding: '0 !important' }}
                />
              </Tooltip>
            </div>
          )}
        </div>
        {show ? (
          <div className="c7ncd-pod-count">
            <FormattedMessage id="ist.expand.count" />
            <span className="c7ncd-pod-count-value">
              {currentPodTargetCount}
            </span>
          </div>
        ) : null}
      </div>
    );
  }
}
