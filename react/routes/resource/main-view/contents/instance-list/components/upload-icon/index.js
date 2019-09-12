import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import { Tooltip, Icon } from 'choerodon-ui';

import './index.less';

/**
 * 分为六种内容：部署中、处理中、部署失败、正常版本、版本升级失败和版本升级
 * - 部署中和版本升级，根据 appVersion 空和非空
 * - 部署失败和版本升级失败，根据 appVersion 空和非空
 * - appVersionId 和 commandVersionId 相同时，均为正常显示
 */
function UploadIcon(props) {
  const ICON_UPLOADING = (<div className="c7n-instance-upload">
    <svg width="16" height="14">
      <path
        className="c7n-instance-upload-arrow"
        d="
              M 5  11
              L 11 11
              L 11 6.5
              L 15 6.5
              L 8  1
              L 1  6.5
              L 5  6.5
              Z
            "
      />
      <line className="c7n-instance-upload-line1" x1="3" y1="10" x2="13" y2="10" />
      <line className="c7n-instance-upload-line2" x1="3" y1="12.5" x2="13" y2="12.5" />
    </svg>
  </div>);

  const {
    intl: { formatMessage },
    dataSource,
  } = props;

  const { appVersion, appVersionId, commandVersionId, commandType, commandVersion, status } = dataSource;

  const CONTENT_DOM = {
    deploying: (av, cv) => (<Fragment>
      <span className="c7n-instance-upload-text">{av || formatMessage({ id: 'ist.deploy.upload' })}</span>
      {cv ? <Tooltip title={formatMessage({ id: `ist.version.${av ? 'upload' : 'deploy'}` }, { text: cv })}>
        {ICON_UPLOADING}
      </Tooltip> : null}
    </Fragment>),
    operating: (av, cv) => (<span className="c7n-instance-upload-text">{formatMessage({ id: 'ist.deploy.delete' })}</span>),
    failed: (av, cv) => (<Fragment>
      <span className="c7n-instance-upload-text">{av || formatMessage({ id: 'ist.deploy.failed' })}</span>
      {cv ? <Tooltip title={formatMessage({ id: `ist.version.${av ? '' : 'deploy.'}failed` }, { text: cv })}>
        <Icon type="error" className="c7n-instance-upload-failed" />
      </Tooltip> : null}
    </Fragment>),
    normal: (av, cv) => (<span className="c7n-instance-upload-text">{cv || av}</span>),
  };

  let type = 'normal';

  if (!appVersionId) {
    switch (status) {
      case 'operating':
        type = 'deploying';
        if (commandType === 'delete') {
          type = 'operating';
        }
        break;
      case 'failed':
        type = 'failed';
        break;
      default:
        type = 'normal';
    }
  } else if (commandVersionId === appVersionId) {
    type = 'normal';
  } else if (commandType === 'update') {
    if (status === 'operating') {
      type = 'deploying';
    } else {
      type = 'failed';
    }
  }

  return (CONTENT_DOM[type](appVersion, commandVersion));
}

UploadIcon.defaultProps = {
  dataSource: {},
};

UploadIcon.propTypes = {
  dataSource: PropTypes.object.isRequired,
};

export default injectIntl(UploadIcon);
