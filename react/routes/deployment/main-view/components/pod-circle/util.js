const SMALL_TYPE = 'small';
const LARGE_TYPE = 'large';

const SMALL_SIZE = 16;
const DEFAULT_SIZE = 24;
const LARGE_SIZE = 70;
const LARGE_STROKE_WIDTH = 5;

const CIRCLE_DEG = 360;

/**
 * svg 尺寸
 * @param size
 * @returns {{width: number, height: number}}
 */
export function getWrapBounds(size) {
  let width = DEFAULT_SIZE;
  let height = DEFAULT_SIZE;

  switch (size) {
    case SMALL_TYPE:
      width = SMALL_SIZE;
      height = SMALL_SIZE;
      break;
    case LARGE_TYPE:
      width = LARGE_SIZE;
      height = LARGE_SIZE;
      break;
    default:
  }

  return {
    width,
    height,
  };
}

/**
 * 圆弧属性
 */
export function getPathAttr(radius, data, total) {
  const strokeWidth = getStrokeWidth(radius);
  const realRadius = radius - strokeWidth;
  let start = 0;

  return data.map(({ value, stroke, name }) => {
    const rate = getRate(value, total);
    const end = start + rate;
    const d = getDirection(radius, radius, realRadius, start, end);

    start = end;

    return {
      name,
      strokeWidth,
      stroke,
      d,
    };
  });
}

/**
 * 计算 path 路径
 * @param x
 * @param y
 * @param radius
 * @param startAngle
 * @param endAngle
 * @returns {string}
 */
export function getDirection(x, y, radius, startAngle, endAngle) {
  const start = polarToCartesian(x, y, radius, endAngle);
  const end = polarToCartesian(x, y, radius, startAngle);

  const largeArcFlag = endAngle - startAngle <= 180 ? '0' : '1';

  return `M ${start.x} ${start.y} A ${radius} ${radius}, 0 ${largeArcFlag}, 0, ${end.x} ${end.y}`;
}

export function getStrokeWidth(radius) {
  return radius < DEFAULT_SIZE ? Math.floor(radius * 0.3) : LARGE_STROKE_WIDTH;
}

export function arraySum(arr, init = 0) {
  return arr.reduce((accu, curr) => accu + curr, init);
}

/**
 * 获取圆弧端点
 * @param centerX
 * @param centerY
 * @param radius
 * @param angleInDegrees
 * @returns {{x: *, y: *}}
 */
function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
  const angleInRadians = (angleInDegrees - 90) * Math.PI / 180;

  return {
    x: centerX + (radius * Math.cos(angleInRadians)),
    y: centerY + (radius * Math.sin(angleInRadians)),
  };
}

/**
 * 圆弧所对的圆心角
 * @param current
 * @param total
 * @returns {number}
 */
function getRate(current, total) {
  return (current / total) * CIRCLE_DEG;
}
