import React, { useEffect, useState, useMemo } from 'react';
import throttle from 'lodash/throttle';

const DELAY_TIME = 100;
const MARGIN = 10;
const MAIN_WIDTH_MIN = 200;
export const X_AXIS_WIDTH_MAX = 320;
export const X_AXIS_WIDTH = 220;

export const useResize = (rootRef, store) => {
  const [bounds, setBounds] = useState({});
  const [draggable, setDraggable] = useState(true);
  const [isDragging, setIsDragging] = useState(false);
  const [resizeNav, setResizeNav] = useState({ x: X_AXIS_WIDTH, y: 0 });

  function getBounds() {
    const { current } = rootRef;
    if (current) {
      const { offsetWidth } = current;
      setBounds({
        width: offsetWidth,
      });
    }
  }
  const limitGetBounds = useMemo(() => throttle(getBounds, DELAY_TIME), []);

  useEffect(() => {
    getBounds();
    window.addEventListener('resize', limitGetBounds, false);
    return () => {
      window.removeEventListener('resize', limitGetBounds);
    };
  }, []);

  function handleDrag(e, data) {
    if (data.deltaX) {
      setResizeNav({ x: data.x, y: data.y });
    }
  }

  function handleStartDrag() {
    setIsDragging(true);
  }

  function handleUnsetDrag() {
    setIsDragging(false);
  }

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
    };

    if (nextResize.x !== resizeNav.x) {
      setResizeNav(nextResize);
    }
    setDraggable(computedLeft > X_AXIS_WIDTH);
    store.setNavBounds(navBounds);
  }, [bounds.width, resizeNav]);

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
