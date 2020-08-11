import { good } from '../interface';

export default class Goods {
    data: good;

    constructor(goods:good = {}) {
      this.data = goods;
    }

    getData() {
      return this.data;
    }

    setData(params: good) {
      this.data = params;
    }
}
