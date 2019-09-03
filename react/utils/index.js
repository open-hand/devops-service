/**
 * 数据请求后的错误拦截
 * 不建议使用此错误处理方法
 * @param data
 * @param hasReturn
 */
function handlePromptError(data, hasReturn = true) {
  if (hasReturn && !data) return false;

  if (data && data.failed) {
    Choerodon.prompt(data.message);
    return false;
  }

  return true;
}

/**
 * 参数 长度低于2则前面加 0，否则不加
 * @param {string | number} str
 * @returns {string}
 */
function padZero(str) {
  return str.toString().padStart(2, '0');
}

/**
 * 格式化时间，转化为 YYYY-MM-DD hh:mm:ss
 * @param {Date} timestamp
 * @returns {string}
 */
function formatDate(timestamp) {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  return `${[year, month, day].map(padZero).join('-')} ${[hour, minutes, seconds].map(padZero).join(':')}`;
}

/**
 * 计算剩余时间
 * @param now 当前时间 时间戳
 * @param end 结束时间 时间戳
 * @returns {string}
 */
function getTimeLeft(now, end) {
  if (now >= end) {
    return '剩余 0 天';
  }
  const resTime = end - now;
  const days = Math.floor(resTime / (24 * 3600 * 1000));
  return `剩余 ${days} 天`;
}

/**
 * 将毫秒数转为时分秒格式
 * @param time 毫秒数
 */
function timeConvert(time) {
  if (!time || typeof time !== 'number') {
    return;
  }
  // 毫秒转为秒
  const now = time / 1000;
  const sec = Math.floor((now % 60) % 60);
  const min = Math.floor(now / 60) % 60;
  const hour = Math.floor(now / 3600);

  let result = `${sec}s`;
  if (hour > 0) {
    result = `${hour}h ${min}m ${sec}s`;
  } else if (hour <= 0 && min > 0) {
    result = `${min}m ${sec}s`;
  }

  return result;
}

function removeEndsChar(str, char) {
  if (typeof str !== 'string') return '';

  return str.endsWith(char) ? str.slice(0, -1) : str;
}

export {
  formatDate,
  getTimeLeft,
  timeConvert,
  handlePromptError,
  removeEndsChar,
};
