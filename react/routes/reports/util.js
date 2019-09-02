import moment from 'moment';
import _ from 'lodash';

/**
 * 返回近7天的时间字符串
 * YYYY-MM-DD
 * @returns {*[]}
 */
function getNear7Day() {
  const dateArr = [];
  for (let i = 0; i < 7; i++) {
    dateArr.push(
      moment()
        .subtract(i, 'days')
        .format('YYYY-MM-DD'),
    );
  }
  return dateArr.reverse();
}

/**
 * 切割补全时间
 * 小于1天返回时间频数
 * 大于1天返回按天的聚合
 * @param start 开始时间
 * @param end 结束时间
 * @param date 时间数组
 * @returns {{}}
 */
function dateSplitAndPad(start, end, date) {
  start = moment(start, 'x');
  end = moment(end, 'x');
  if (start > end) {
    return {};
  }
  let dateArr = {};
  const timeDiff = end - start;
  if (timeDiff < 3600 * 25 * 1000) {
    const oneDay = _.countBy(date, (item) => item.slice(0, 10));
    dateArr = !_.isEmpty(oneDay)
      ? oneDay
      : { [moment().format('YYYY-MM-DD')]: 0 };
  } else {
    const days = timeDiff / (3600 * 24 * 1000);
    const dateGroup = _.countBy(date, (item) => item.slice(0, 10));
    for (let i = 0; i <= Math.floor(days); i++) {
      const d = moment(end)
        .subtract(i, 'days')
        .format('YYYY-MM-DD');
      if (dateGroup[d] || dateGroup[d] === 0) {
        dateArr[d] = dateGroup[d];
      } else {
        dateArr[d] = 0;
      }
    }
  }
  return dateArr;
}


/**
 * 提取出对象的键和值，并形成对应的两个数组
 * @param obj
 * @param obj
 * @returns {*}
 */
function pickEntries(obj) {
  if (Object.prototype.toString.call(obj) !== '[object Object]') {
    return {};
  }
  const keys = Object.keys(obj);
  const values = keys.map((item) => obj[item]);
  return {
    keys,
    values,
  };
}

/**
 * 返回次数报表横纵坐标数组
 * @param startTime 开始时间 时间戳
 * @param endTime 结束时间 时间戳
 * @param oldxAxis 横坐标数据 数组
 * @param oldyAxis 纵坐标数据 {a: [], b: [], ...}
 * @returns {xAixs: [], yAxis: {a: [], b: [], ...}}
 */
function getAxis(startTime, endTime, oldxAxis = [], oldyAxis = {}) {
  const xAxis = [];
  for (; startTime <= endTime; startTime += 86400000) {
    const tmp = new Date(startTime);
    xAxis.push(
      `${tmp.getFullYear()}-${padZero(tmp.getMonth() + 1)}-${padZero(
        tmp.getDate(),
      )}`,
    );
  }
  const yAxis = {};
  _.foreach(oldyAxis, (value, key) => {
    yAxis[key] = [];
    const data = oldyAxis[key] || [];
    if (oldxAxis.length) {
      _.map(oldxAxis, (str, index) => {
        yAxis[key][xAxis.indexOf(str)] = data[index];
      });
    }
  });
  return { xAxis, yAxis };
}

/**
 * 参数 长度低于2则前面加 0，否则不加
 * @param {string | number} str
 * @returns {string}
 */
function padZero(str) {
  return str.toString().padStart(2, '0');
}

export {
  getNear7Day,
  dateSplitAndPad,
  pickEntries,
  getAxis,
  padZero,
};
