import React, { useEffect, useState } from 'react';
import throttle from 'lodash/throttle';

const MARGIN = 10;
const MAIN_WIDTH_MIN = 200;

export const X_AXIS_WIDTH_MAX = 320;
export const X_AXIS_WIDTH = 220;

export const useResize = (rootRef, store) => {
  const [bounds, setBounds] = useState({});
  const [draggable, setDraggable] = useState(true);
  const [isDragging, setIsDragging] = useState(false);
  const [resizeNav, setResizeNav] = useState({ x: X_AXIS_WIDTH, y: 0 });

  useEffect(() => {
    const getBounds = () => {
      const { current } = rootRef;

      if (current) {
        const { offsetWidth, offsetHeight } = current;

        setBounds({
          width: offsetWidth,
          height: offsetHeight,
        });
      }
    };
    getBounds();

    const limitGetBounds = throttle(getBounds, 100);
    window.addEventListener('resize', limitGetBounds, true);
    return () => {
      window.removeEventListener('resize', limitGetBounds);
    };
  }, [rootRef]);

  const handleDrag = (e, data) => {
    if (data.deltaX) {
      setResizeNav({ x: data.x, y: data.y });
    }
  };

  const handleStartDrag = () => {
    setIsDragging(true);
  };

  const handleUnsetDrag = () => {
    setIsDragging(false);
  };

  useEffect(() => {
    const navX = resizeNav.x;
    const computedLeft = bounds.width - MAIN_WIDTH_MIN;
    let nextResize = resizeNav;

    if (computedLeft < X_AXIS_WIDTH || navX < X_AXIS_WIDTH) {
      nextResize = {
        ...resizeNav,
        x: X_AXIS_WIDTH,
      };
    } else if (navX > X_AXIS_WIDTH_MAX) {
      nextResize = {
        ...resizeNav,
        x: X_AXIS_WIDTH_MAX,
      };
    } else if (computedLeft < navX) {
      nextResize = {
        ...resizeNav,
        x: computedLeft,
      };
    }

    const navBounds = {
      width: nextResize.x + MARGIN,
      height: bounds.height,
    };

    setResizeNav(nextResize);
    setDraggable(computedLeft > X_AXIS_WIDTH);
    store.setNavBounds(navBounds);
  }, [bounds.height, bounds.width, resizeNav, store]);

  return {
    bounds,
    draggable,
    isDragging,
    resizeNav,
    handleDrag,
    handleStartDrag,
    handleUnsetDrag,
  };
};
