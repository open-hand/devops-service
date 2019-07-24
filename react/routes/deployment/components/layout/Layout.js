import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import Draggable from 'react-draggable';
import ScrollArea from '../scroll-area';

import './layout.less';

const MARGIN = 10;
const X_AXIS_WIDTH = 220;
const X_AXIS_WIDTH_MAX = 400;
const MAIN_WIDTH_MIN = 500;

export const Pane = ({ isPane = true, hidden, animate, border, children, style, className = '' }) => {
  const styled = classnames({
    'c7n-draggers-pane': isPane,
    'c7n-draggers-visible': !hidden,
    'c7n-draggers-animate': animate,
    [`c7n-draggers-border-${border}`]: !!border,
    [className]: className,
  });
  return <div className={styled} style={style}>{children}</div>;
};

export const Nav = ({ hidden, children, position, ...props }) => (hidden ? null : (
  <Pane
    style={position}
    border="right"
    {...props}
  >
    {children}
  </Pane>
));

Nav.propTypes = {
  hidden: PropTypes.bool,
  children: PropTypes.node.isRequired,
  position: PropTypes.shape({}),
};
Nav.defaultProps = {
  hidden: false,
  position: undefined,
};

export const Main = ({ children, position, ...props }) => (
  <Pane
    className="c7n-deployment-layout-main"
    style={position}
    {...props}
  >
    <ScrollArea vertical horizontal>
      {children}
    </ScrollArea>
  </Pane>
);

Main.propTypes = {
  children: PropTypes.node.isRequired,
  position: PropTypes.shape({}),
};
Main.defaultProps = {
  position: undefined,
};

const getPositionWithRange = (number, max, min) => {
  if (number <= max && number >= min) {
    return number;
  } else if (number > max) {
    return max;
  } else {
    return min;
  }
};

const getMainPosition = ({ bounds, resizeNav, isNavHidden, margin }) => {
  const left = getPositionWithRange(resizeNav.x, X_AXIS_WIDTH_MAX, X_AXIS_WIDTH) + margin;
  const navX = isNavHidden ? 25 : left;

  return {
    left: navX,
    top: 0,
    width: bounds.width - navX,
  };
};

export class Layout extends Component {
  static propTypes = {
    children: PropTypes.func.isRequired,
    bounds: PropTypes.shape({
      width: PropTypes.number.isRequired,
      height: PropTypes.number.isRequired,
    }).isRequired,
    options: PropTypes.shape({
      showNav: PropTypes.bool.isRequired,
    }).isRequired,
  };

  state = {
    isDragging: false,
    resizeNav: { x: X_AXIS_WIDTH, y: 0 },
  };

  static getDerivedStateFromProps(props, state) {
    const { bounds, options } = props;
    const { resizeNav } = state;

    const isNavHidden = !options.showNav;

    const navX = resizeNav.x;
    const computedLeft = bounds.width - MAIN_WIDTH_MIN;

    const mutation = {};

    if (!isNavHidden) {
      if (computedLeft < X_AXIS_WIDTH || navX < X_AXIS_WIDTH) {
        mutation.resizeNav = {
          x: X_AXIS_WIDTH,
          y: 0,
        };
      } else if (navX > X_AXIS_WIDTH_MAX) {
        mutation.resizeNav = {
          x: X_AXIS_WIDTH_MAX,
          y: 0,
        };
      } else if (computedLeft < navX) {
        mutation.resizeNav = {
          x: computedLeft,
          y: 0,
        };
      }
    }

    return mutation.resizeNav ? { ...state, ...mutation } : state;
  }

  setResizeNav = (e, data) => {
    if (data.deltaX) {
      this.setState({ resizeNav: { x: data.x, y: data.y } });
    }
  };

  setDragNav = () => {
    this.setState({ isDragging: 'nav' });
  };

  unsetDrag = () => {
    this.setState({ isDragging: false });
  };

  render() {
    const {
      children,
      bounds,
      options: {
        showNav,
      },
    } = this.props;
    const {
      isDragging,
      resizeNav,
    } = this.state;

    const isNavHidden = !showNav;

    const navX = resizeNav.x;
    const navWidth = isNavHidden ? 25 : navX + MARGIN;

    const draggableClass = classnames({
      'c7n-draggers-handle': true,
      'c7n-draggers-handle-dragged': isDragging,
    });

    return bounds ? (
      <Fragment>
        {isNavHidden ? null : (
          <Draggable
            disabled={isNavHidden}
            axis="x"
            position={resizeNav}
            bounds={{
              left: X_AXIS_WIDTH,
              top: 0,
              right: navX >= X_AXIS_WIDTH_MAX ? X_AXIS_WIDTH_MAX : bounds.width - X_AXIS_WIDTH,
              bottom: 0,
            }}
            onStart={this.setDragNav}
            onDrag={this.setResizeNav}
            onStop={this.unsetDrag}
          >
            <div className={draggableClass} />
          </Draggable>
        )}
        {isDragging && showNav ? <div className="c7n-draggers-blocker" /> : null}
        {children({
          mainProps: {
            animate: !isDragging,
            position: getMainPosition({ bounds, resizeNav, isNavHidden, margin: MARGIN }),
          },
          navProps: {
            animate: !isDragging,
            hidden: isNavHidden,
            position: {
              height: bounds.height,
              left: 0,
              top: 0,
              width: navWidth,
            },
          },
        })}
      </Fragment>
    ) : null;
  }
}
