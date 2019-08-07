import React, { PureComponent, Fragment } from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { withRouter, Link } from 'react-router-dom';
import { stores } from '@choerodon/boot';
import _ from 'lodash';
import { Tooltip, Button } from 'choerodon-ui';
import './PodCircle.scss';

const { AppState } = stores;

@withRouter
@injectIntl
export default class PodCircle extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      btnDisable: false,
      textDisplay: false,
    };
  }

  /**
   * 限制连续点击发送请求的次数
   * 限制600ms
   *
   * @memberof PodCircle
   */
  operatePodCount = _.debounce((count) => {
    const { id: projectId } = AppState.currentMenuType;
    const { currentPage, store, envId, name } = this.props;
    const page = currentPage === 'env-overview' ? 'overview' : 'instance';
    store.operatePodCount(page, projectId, envId, name, count);
  }, 600);

  /**
   * 环形图下的文字显示
   * @memberof PodCircle
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

    if (targetCount > 1) {
      const count = currentPodTargetCount - 1;
      // 最小pod数为1
      if (count <= 1) {
        btnDisable = true;
      }
      this.changeTextDisplay();
      this.setState({ btnDisable });
      this.operatePodCount(count);
      handleChangeCount(
        _.assign({}, targetCount, { [`${name}-${podType}`]: count })
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
      _.assign({}, targetCount, { [`${name}-${podType}`]: count })
    );
  };

  /**
   * 获取 pod 的环形图
   * @readonly
   * @memberof PodCircle
   */
  get renderCircle() {
    const {
      count: { sum, correct, correctCount },
    } = this.props;
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
      id: projectId,
      name: projectName,
      organizationId,
      type,
    } = AppState.currentMenuType;
    const {
      podType,
      status,
      handleLink,
      currentPage,
      appId,
      instanceId,
      connect,
      name,
      count: { sum },
      targetCount,
    } = this.props;
    const { btnDisable, textDisplay } = this.state;

    const currentPodTargetCount = targetCount[`${name}-${podType}`] || sum;

    // 实际pod数和目标数不同
    // 修改过pod数
    const show = textDisplay
      && sum !== currentPodTargetCount
      && connect
      && status === 'running';
    const descIsEnable = btnDisable
      || !connect
      || currentPodTargetCount <= 1
      || status !== 'running';

    const backPath = `/devops/${currentPage}?type=${type}&id=${projectId}&name=${encodeURIComponent(
      projectName
    )}&organizationId=${organizationId}`;
    const state = { appId, backPath, instanceId };

    return (
      <Fragment>
        <div className="c7ncd-pod-wrap">
          <div className="c7ncd-pod-content">
            {status === 'running' ? (
              <Link
                to={{
                  pathname: '/devops/container',
                  search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(
                    projectName
                  )}&organizationId=${organizationId}`,
                  state,
                }}
                onClick={handleLink}
              >
                <Tooltip title={<FormattedMessage id="ist.expand.link" />}>
                  {this.renderCircle}
                </Tooltip>
              </Link>
            ) : (
              this.renderCircle
            )}
          </div>
          {podType === 'deploymentDTOS' && (
            <div className="c7ncd-pod-content c7ncd-pod-btn-wrap">
              <Button
                disabled={!(connect && status === 'running')}
                className="c7ncd-pod-btn"
                size="small"
                icon="expand_less"
                onClick={this.handleIncrease}
              />
              <Button
                disabled={descIsEnable}
                className="c7ncd-pod-btn"
                size="small"
                icon="expand_more"
                onClick={this.handleDecrease}
              />
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
      </Fragment>
    );
  }
}
