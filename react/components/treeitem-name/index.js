import React, { Fragment, memo } from 'react';
import PropTypes from 'prop-types';
import toUpper from 'lodash/toUpper';
import classnames from 'classnames';

import './index.less';

const TreeItemName = memo(({
  name, search, disabled, headSpace,
}) => {
  const index = toUpper(name).indexOf(toUpper(search));
  const beforeStr = name?.substr(0, index);
  const currentStr = name?.substr(index, search.length);
  const afterStr = name?.substr(index + search.length);

  const textClass = classnames({
    'c7ncd-treemenu-text': true,
    'c7ncd-treemenu-text-disabled': disabled,
    'c7ncd-treemenu-text-ml': headSpace,
  });

  return (
    <span className={textClass}>
      {index > -1 ? (
        <>
          {beforeStr}
          <span className="c7ncd-treemenu-text-highlight">{currentStr}</span>
          {afterStr}
        </>
      ) : name}
      {disabled && <i className="c7ncd-treemenu-disabled" />}
    </span>
  );
});

TreeItemName.propTypes = {
  name: PropTypes.string.isRequired,
  search: PropTypes.string,
  disabled: PropTypes.bool,
  headSpace: PropTypes.bool,
};

TreeItemName.defaultProps = {
  disabled: false,
  headSpace: true,
  search: '',
};

export default TreeItemName;
