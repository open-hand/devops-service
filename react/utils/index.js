/**
 * 【废弃】请使用 handlePromptError
 */
function handleProptError(data) {
  if (data && data.failed) {
    Choerodon.prompt(data.message);
    return false;
  }
  return data;
}

/**
 * 【废弃】请使用 handlePromptError
 */
function handleCheckerProptError(data) {
  if (data && data.failed) {
    Choerodon.prompt(data.message);
    return false;
  }
  return true;
}

/**
 * 数据请求后的错误拦截
 * 【将要废弃】请使用框架内置的错误处理方法
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

export {
  handleProptError,
  formatDate,
  getTimeLeft,
  handleCheckerProptError,
  handlePromptError,
};
