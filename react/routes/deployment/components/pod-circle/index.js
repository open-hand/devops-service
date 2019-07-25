import React, { memo } from 'react';
import PropTypes from 'prop-types';
import { getWrapBounds, getPathAttr, getStrokeWidth, arraySum } from './util';

const BASE_COLOR = '#e3e3e3';

export const DrawArc = ({ d, stroke, strokeWidth }) => <path
  fill="none"
  stroke={stroke}
  strokeWidth={strokeWidth}
  d={d}
/>;

DrawArc.propTypes = {
  d: PropTypes.string.isRequired,
  stroke: PropTypes.string.isRequired,
  strokeWidth: PropTypes.number.isRequired,
};

export const DrawText = ({ text, y, isBold, ...props }) => (<text
  x="50%"
  y={y}
  {...props}
>
  {isBold ? <tspan fontWeight="bold">{text}</tspan> : text}
</text>);

DrawText.propTypes = {
  y: PropTypes.number,
  isBold: PropTypes.bool,
};

DrawText.defaultProps = {
  y: 0,
  isBold: false,
};

export const DrawSvg = ({ bounds, children }) => <svg
  xmlns="http://www.w3.org/2000/svg"
  {...bounds}
>
  {children}
</svg>;

DrawSvg.propTypes = {
  bounds: PropTypes.shape({
    width: PropTypes.number,
    height: PropTypes.number,
  }),
};

DrawSvg.defaultProps = {
  bounds: {},
};

export const DrawCircle = ({ bounds, radius, color = BASE_COLOR }) => {
  const strokeWidth = getStrokeWidth(radius);
  const realRadius = radius - strokeWidth;

  return <DrawSvg bounds={bounds}>
    <circle
      cx="50%"
      cy="50%"
      fill="none"
      r={realRadius}
      stroke={color}
      strokeWidth={strokeWidth}
    />
  </DrawSvg>;
};

DrawCircle.propTypes = {
  bounds: PropTypes.shape({
    width: PropTypes.number,
    height: PropTypes.number,
  }),
};

DrawCircle.defaultProps = {
  bounds: {},
};

/**
 * 圆环有两种：circle 绘制的圆 | path 绘制圆弧拼接
 * 只有一种数据源不为0时使用 circle
 * 多种数据都不为0时使用 path
 */
const PodCircle = memo(({ size, style, dataSource }) => {
  const bounds = {
    ...getWrapBounds(size),
    ...style,
  };

  const width = bounds.width;
  const radius = width / 2;

  if (!Array.isArray(dataSource) || !dataSource.length) {
    return <DrawCircle bounds={bounds} radius={radius} />;
  }

  const values = dataSource.map(({ value }) => value);
  const total = arraySum(values);

  if (!total) {
    return <DrawCircle bounds={bounds} radius={radius} />;
  } else if (values.includes(total)) {
    const circle = dataSource.find(({ value }) => (value === total));
    return <DrawCircle bounds={bounds} radius={radius} color={circle.stroke} />;
  } else {
    const pathAttr = getPathAttr(radius, dataSource, total);
    return <DrawSvg bounds={bounds}>
      {pathAttr.map(({ name, ...props }) => <DrawArc key={name} {...props} />)}
    </DrawSvg>;
  }
});

PodCircle.propTypes = {
  size: PropTypes.string,
  style: PropTypes.shape({
    width: PropTypes.number,
    height: PropTypes.number,
  }),
  dataSource: PropTypes.array,
};

PodCircle.defaultProps = {
  style: {},
  dataSource: [],
};

export default memo(PodCircle);
