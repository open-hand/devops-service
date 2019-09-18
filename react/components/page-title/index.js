import React from 'react';
import PropTypes from 'prop-types';
import { Progress } from 'choerodon-ui';

import './index.less';

export default function PageTitle({ fallback, content }) {
  return <div className="c7ncd-page-title">
    {content || fallback || <Progress type="loading" size="small" />}
  </div>;
}

PageTitle.propTypes = {
  fallback: PropTypes.any,
  content: PropTypes.any,
};
