import React, { useEffect, useState } from 'react';
import Goods from '@/routes/ddd/domain/goods-domain/entities/goods';
import GoodsService from './Services/goodsService';

export default () => {
  const [goodsList, setGoodsList] = useState<Goods[]>([]);

  useEffect(() => {
    GoodsService.getGoodsList().then((res: Goods[]) => {
      setGoodsList(res);
    });
  }, []);

  const handleClickPrice = (index: number): void => {
    setGoodsList(goodsList.map((g, gIndex) => {
      if (gIndex === index) {
        g.setData({
          ...g.getData(),
          price: '',
        });
      }
      return g;
    }));
  };

  console.log(goodsList);

  const renderGoods = () => goodsList.map((g, index: number) => {
    const data = g.getData();
    return [
      <p>
        name:
        {data.name}
      </p>,
      <button type="button" onClick={() => handleClickPrice(index)}>
        price:
        {data.price}
      </button>,
    ];
  });

  return (
    <div>{renderGoods()}</div>
  );
};
