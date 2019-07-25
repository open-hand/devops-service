/**
 * @author ale0720@163.com
 * @date 2019-05-14 20:46
 */

import React from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Tooltip } from 'choerodon-ui';

import './UserList.scss';

function isEmpty(data) {
  return !data.length;
}

export default function UserList({ type, dataSource }) {
  let content = null;

  switch (type) {
    case 'handler':
      content = <FormattedMessage id="notification.target.handler" />;
      break;
    case 'owner':
      content = <FormattedMessage id="notification.target.owner" />;
      break;
    case 'specifier':
      const hasUser = dataSource ? !isEmpty(dataSource) : false;
      if (hasUser) {
        content = dataSource.map(({ imageUrl, realName, userId }) => (<Tooltip
          trigger="hover"
          placement="top"
          title={realName || ''}
        >
          {
            imageUrl
              ? <img
                className="c7n-devops-userlist"
                src={imageUrl}
                alt=""
              />
              : <span
                className="c7n-devops-userlist"
                key={userId}
              >
                {(realName || '').substr(0, 1).toUpperCase()}
              </span>
          }
        </Tooltip>));
      }
      break;
    default:
  }

  return content;
}

UserList.propTypes = {
  type: PropTypes.string.isRequired,
  dataSource: PropTypes.array,
};
