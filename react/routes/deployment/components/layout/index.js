import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import * as L from './Layout';

const LayoutPage = React.memo(
  ({ width, height, Nav, Content, options }) => (width && height
    ? (<L.Layout
      options={options}
      bounds={{ width, height }}
    >
      {({ navProps, mainProps }) => (
        <Fragment>
          <L.Nav {...navProps}>
            <Nav navBounds={navProps.position} />
          </L.Nav>
          <L.Main {...mainProps}>
            <Content bounds={mainProps.position} />
          </L.Main>
        </Fragment>
      )}
    </L.Layout>)
    : null),
);

LayoutPage.displayName = 'LayoutPage';
LayoutPage.propTypes = {
  width: PropTypes.number,
  height: PropTypes.number,
  Nav: PropTypes.any.isRequired, // eslint-disable-line react/forbid-prop-types
  Content: PropTypes.any.isRequired, // eslint-disable-line react/forbid-prop-types
  options: PropTypes.shape({
    showNav: PropTypes.bool.isRequired,
  }).isRequired,
};
LayoutPage.defaultProps = {
  height: 0,
  width: 0,
};

export default LayoutPage;
