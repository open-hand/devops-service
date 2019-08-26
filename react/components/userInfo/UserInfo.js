import React, { PureComponent, Fragment } from 'react';
import PropTypes from 'prop-types';

import './UserInfo.less';

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
      : <span className="c7ncd-userinfo-avatar-txt">{name.toUpperCase().substring(0, 1)}</span>;

    return (
      <div className="c7ncd-userinfo-wrap">
        {name && (<Fragment>
          {ava}
          <div className="c7ncd-userinfo-name">
            {id || ''}
            {name}
          </div>
        </Fragment>)}
      </div>
    );
  }
}

export default UserInfo;
