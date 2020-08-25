import { axiosGetGoodsList } from '../domain/goods-domain/data-source/apis';
import Goods from '../domain/goods-domain/entities/goods';

class GoodsService {
  static getGoodsList() {
    return axiosGetGoodsList().then((res) => res.map((r) => new Goods(r)));
  }
}

export default GoodsService;
