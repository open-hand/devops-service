/**
 * @author ale0720@163.com
 * @date 2019-05-14 21:21
 */
import React from 'react';
import PropTypes from 'prop-types';

import './TableTags.scss';

export default function TableTags({ value }) {
  return (<span className="c7n-devops-tabletags">{value}</span>);
}

TableTags.propTypes = {
  name: PropTypes.string,
};
