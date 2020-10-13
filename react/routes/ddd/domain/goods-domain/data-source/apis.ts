import { good } from '../interface';
import { goodTranslator } from './translators';

export function axiosGetGoodsList(): Promise<Array<good>> {
  return new Promise((resolve) => {
    resolve([{
      id: 1,
      name: '苹果',
      price: 3,
    }, {
      id: 2,
      name: '香蕉',
      price: 2,
    }].map((i) => goodTranslator(i)));
  });
}
