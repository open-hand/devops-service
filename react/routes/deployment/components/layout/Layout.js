import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import Draggable from 'react-draggable';
import ScrollArea from '../../../../components/scroll-area';

import './layout.less';

const MARGIN = 10;
const X_AXIS_WIDTH = 220;
const X_AXIS_WIDTH_MAX = 320;
const MAIN_WIDTH_MIN = 200;

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
    draggable: true,
    resizeNav: { x: X_AXIS_WIDTH, y: 0 },
  };

  static getDerivedStateFromProps(props, state) {
    const { bounds, options } = props;
    const { resizeNav } = state;

    const isNavHidden = !options.showNav;

    /*
     * NOTE: 设置左侧导航栏的可拖动边界
     *
     * MAIN_WIDTH_MIN 是左边内容区域的最小宽度，为内容可见时的最小宽度
     * 整个 layout 的宽度（通过外层组件传入的 bounds）减去内容区域就可得到左侧导航栏可用宽度
     *
     * 当左侧可用宽度小于导航栏初始宽度 X_AXIS_WIDTH，或者拖拽控件x坐标小于 X_AXIS_WIDTH，则不允许进行拖拽
     * 当左侧可用宽度大于导航栏最大允许宽度 X_AXIS_WIDTH_MAX，则不允许进行拖拽
     *
     * 如果出现拖拽控件单独显示的情况和拖拽时页面跳动，请调整此处的值
     */
    const navX = resizeNav.x;
    const computedLeft = bounds.width - MAIN_WIDTH_MIN;
    const mutation = {};
    let draggable = true;

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

      if (computedLeft <= X_AXIS_WIDTH) {
        draggable = false;
      }
    } else {
      draggable = false;
    }

    return mutation.resizeNav ? { ...state, ...mutation, draggable } : { ...state, draggable };
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
      draggable,
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
        {!draggable ? null : (
          <Fragment>
            <Draggable
              // disabled={!draggable}
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
            {isDragging ? <div className="c7n-draggers-blocker" /> : null}
          </Fragment>
        )}
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
