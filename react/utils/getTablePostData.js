import isEmpty from 'lodash/isEmpty';
import omit from 'lodash/omit';

/**
 * 表格搜索数据，并在初始化请求无相应字段时自动添加
 * @param data
 * @returns {{searchParam, params: Array}}
 */
export default (data) => {
  let params = [];
  let searchParam = {};

  if (!isEmpty(data)) {
    params = data.params;
    searchParam = omit(data, ['params']);
  }

  return {
    params,
    searchParam,
  };
};
