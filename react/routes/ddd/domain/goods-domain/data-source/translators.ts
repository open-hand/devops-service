import { good } from '../interface';

interface apiGood extends Omit<good, 'price'> {
    price?: number,
}

export function goodTranslator(data: apiGood): good {
  function abs(val: number): string {
    // 金额转换 分->元 保留2位小数 并每隔3位用逗号分开 1,234.56
    const str = `${(val / 100).toFixed(2)}`;
    const intSum = str.substring(0, str.indexOf('.')).replace(/\B(?=(?:\d{3})+$)/g, ',');// 取到整数部分
    const dot = str.substring(str.length, str.indexOf('.'));// 取到小数部分搜索
    const ret = intSum + dot;
    return ret;
  }
  const price: any = data.price && abs(data.price);
  return {
    ...data,
    price,
  };
}
