import React, { PureComponent, Fragment } from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
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
  };

  static defaultProps = {
    showName: true,
    size: 'small',
  };

  render() {
    const { avatar, name, id, showName, size } = this.props;
    
    const ava = avatar

      ? <img src={avatar} alt="avatar" className={classnames('c7ncd-userinfo-avatar', `c7ncd-userinfo-avatar-${size}`)} />
      : <span className={classnames('c7ncd-userinfo-avatar-txt', `c7ncd-userinfo-avatar-${size}`)}>{(name || '').toUpperCase().substring(0, 1)}</span>;

    return (
      <div className="c7ncd-userinfo-wrap">
        {name && (<Fragment>
          <Tooltip title={`${name}${id ? ` (${id})` : ''}`}>
            {ava}
          </Tooltip>
          {showName ? <div className="c7ncd-userinfo-name">
            {name}
          </div> : null}
        </Fragment>)}
      </div>
    );
  }
}

export default UserInfo;
