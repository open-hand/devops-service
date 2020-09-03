import React, { PureComponent, Fragment } from 'react';
import PropTypes from 'prop-types';
import './UserInfo.less';
import { Tooltip } from 'choerodon-ui';

class UserInfo extends PureComponent {
  static propTypes = {
    name: PropTypes.string.isRequired,
    avatar: PropTypes.string,
    size: PropTypes.string,
    id: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
    ]),
    showName: PropTypes.bool,
    showTooltip: PropTypes.bool,
    className: PropTypes.string,
  };

  static defaultProps = {
    showName: true,
    showTooltip: true,
    size: 'small',
  };

  render() {
    const { avatar, name, id, showName, showTooltip, size, className } = this.props;
    
    const ava = avatar

      ? <img src={avatar} alt="avatar" className={`c7ncd-userinfo-avatar c7ncd-userinfo-avatar-${size}`} />
      : <span className={`c7ncd-userinfo-avatar-txt c7ncd-userinfo-avatar-${size}`}>{(name || '').toUpperCase().substring(0, 1)}</span>;

    return (
      <div className={`c7ncd-userinfo-wrap ${className || ''}`}>
        {name && (<Fragment>
          {showTooltip ? <Tooltip title={`${name}${id ? ` (${id})` : ''}`}>
            {ava}
          </Tooltip> : ava}
          {showName ? <div className="c7ncd-userinfo-name">
            {name}
          </div> : null}
        </Fragment>)}
      </div>
    );
  }
}

export default UserInfo;
