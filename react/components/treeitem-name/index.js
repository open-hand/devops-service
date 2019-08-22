import React, { Fragment, memo } from 'react';
import PropTypes from 'prop-types';
import toUpper from 'lodash/toUpper';

import './index.less';

const TreeItemName = memo(({ name, search }) => {
  const index = toUpper(name).indexOf(toUpper(search));
  const beforeStr = name.substr(0, index);
  const currentStr = name.substr(index, search.length);
  const afterStr = name.substr(index + search.length);

  return <span className="c7ncd-treemenu-text">
    {index > -1 ? <Fragment>
      {beforeStr}
      <span className="c7ncd-treemenu-text-highlight">{currentStr}</span>
      {afterStr}
    </Fragment> : name}
  </span>;
});

TreeItemName.propTypes = {
  name: PropTypes.string.isRequired,
  search: PropTypes.string,
};

export default TreeItemName;
