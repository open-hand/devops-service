import React, { PureComponent, Fragment } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Icon, Tooltip } from 'choerodon-ui';
import classnames from 'classnames';
import _ from 'lodash';
import { timeConvert } from '../../../../../../utils/utils';
import { STAGE_FLOW_MANUAL, STATUS_ICON } from '../Constants';

import './index.less';

export default class DetailTitle extends PureComponent {
  static propTypes = {
    name: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired,
    time: PropTypes.number,
    status: PropTypes.string,
    isCadence: PropTypes.bool,
    head: PropTypes.bool,
    onlyOne: PropTypes.bool,
    tail: PropTypes.bool,
  };

  static defaultProps = {
    head: false,
    onlyOne: false,
    tail: false,
  };

  render() {
    const {
      name,
      time,
      type,
      user,
      status,
      checking,
      isCadence,
      head,
      onlyOne,
      tail,
    } = this.props;

    /**
     * 手动流转模式说明
     * 待流转，未开启审核： 所有 user 的 audit 属性为 false，checking 属性为 null
     * 待流转，已开启审核： 所有 user 的 audit 属性为 false，checking 属性为 true
     * 已审核，通过：             只要有 audit 属性为 true，status   属性为 success
     * 已审核，终止：             只要有 audit 属性为 true，status   属性为 stop
     */
    let triggerDom = null;

    if (type === STAGE_FLOW_MANUAL) {
      const audit = _.find(user, 'audit');
      if (audit) {
        const isStopFlow = status === 'stop' || (isCadence && checking);

        const messageCode = isStopFlow ? 'stopped' : type;
        const spanClass = classnames({
          'c7ncd-manualflow-pass': !isStopFlow,
          'c7ncd-manualflow-stop': isStopFlow,
        });

        const { realName, imageUrl, loginName } = audit;
        triggerDom = <Fragment>
          <Tooltip title={realName || loginName}>
            {imageUrl
              ? <img className="c7ncd-trigger-img" src={imageUrl} alt="avatar" />
              : <span className="c7ncd-trigger-text">{_.toString(realName || loginName).toUpperCase().substring(0, 1)}</span>}
          </Tooltip>
          <span className={spanClass}><FormattedMessage id={`pipeline.flow.${messageCode}`} /></span>
        </Fragment>;
      } else {
        const userName = _.map(user, ({ realName, loginName }) => realName || loginName).join('，');
        const spanClass = classnames({
          'c7ncd-manualflow-pending': !!checking,
        });
        triggerDom = <Tooltip title={userName}>
          <span className={spanClass}>
            <FormattedMessage id={`pipeline.flow.${type}`} />
          </span>
        </Tooltip>;
      }
    } else {
      const spanClass = classnames({
        'c7ncd-autoflow-pass': status === 'success',
      });
      triggerDom = <span className={spanClass}>
        <FormattedMessage id={`pipeline.flow.${type}`} />
      </span>;
    }

    const statusStyle = classnames({
      'c7ncd-pipeline-status': true,
      [`c7ncd-pipeline-status_${status}`]: true,
    });
    const bkColor = classnames({
      'c7ncd-pipeline-title': true,
      'c7ncd-pipeline-title-head': head,
      'c7ncd-pipeline-title-tail': onlyOne || tail,
      [`c7ncd-pipeline-title_${status}`]: true,
    });

    let arrowDom = null;
    if (!onlyOne) {
      const arrowHeadClass = classnames({
        'arrow-head': true,
        [`arrow-head_${status}`]: true,
      });
      const arrowTailClass = classnames({
        'arrow-tail': true,
        [`arrow-tail_${status}`]: true,
      });

      arrowDom = <Fragment>
        <div className={arrowHeadClass} />
        <div className={arrowTailClass} />
      </Fragment>;
    }

    return (
      <div className={bkColor}>
        {arrowDom}
        <div className={statusStyle}>
          <Icon className="stage-icon" type={STATUS_ICON[status]} />
        </div>
        <div className="c7ncd-pipeline-detail-execute">
          <div className="c7ncd-pipeline-execute-name">{name}</div>
          <div className="c7ncd-pipeline-execute-time">{timeConvert(Number(time))}</div>
        </div>
        {!tail && <div className="c7ncd-pipeline-title-trigger">
          {triggerDom}
        </div>}
      </div>
    );
  }
}
