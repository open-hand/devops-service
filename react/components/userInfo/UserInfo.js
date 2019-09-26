import React, { PureComponent, Fragment } from 'react';
import PropTypes from 'prop-types';

import './UserInfo.less';
import { Tooltip } from 'choerodon-ui';

class UserInfo extends PureComponent {
  static propTypes = {
    name: PropTypes.string.isRequired,
    avatar: PropTypes.string,
    id: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
    ]),
  };

  render() {
    const { avatar, name, id } = this.props;
    const ava = avatar
      ? <img src={avatar} alt="avatar" className="c7ncd-userinfo-avatar" />
      : <span className="c7ncd-userinfo-avatar-txt">{(name || '').toUpperCase().substring(0, 1)}</span>;

    return (
      <div className="c7ncd-userinfo-wrap">
        {name && (<Fragment>
          <Tooltip title={`${name}${id ? ` (${id})` : ''}`}>
            {ava}
          </Tooltip>
          <div className="c7ncd-userinfo-name">
            {name}
          </div>
        </Fragment>)}
      </div>
    );
  }
}

export default UserInfo;
