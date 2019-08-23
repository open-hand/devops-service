/**
 *
 * 设置默认选中项和选中项丢失后的处理
 *
 *   记录中没有选中项或者选中项和store中保存的项不匹配 --则需要进行重新选择
 *
 *      先从记录中找到匹配的项，找到则直接设置为选中
 *      否则查找该项的父节点设置为选中
 *
 *   如果无父节点则选中所有记录中的第一项
 *
 *   否则清空选中
 *
 * 要求 Record 中必须包含字段
 *   key 当前节点的标识
 *   parentId 父节点标识
 *
 * */

export default (dataSet, store) => {
  if (dataSet.length) {
    const selectedRecord = dataSet.find((record) => record.isSelected);
    const { key: selectedKey, parentId: selectedParentId } = store.getSelectedMenu;

    if (!selectedRecord || selectedRecord.get('key') !== selectedKey) {
      const nextSelected = dataSet.find((record) => record.get('key') === selectedKey);
      if (nextSelected) {
        nextSelected.isSelected = true;
      } else {
        const parent = dataSet.find((record) => record.get('key') === selectedParentId);
        const parentData = dataSet.toData();
        store.setSelectedMenu(parentData);
        if (parent) {
          parent.isSelected = true;
        } else {
          const first = dataSet.get(0);
          if (first) {
            first.isSelected = true;
            const firstData = first.toData();
            store.setSelectedMenu(firstData);
          } else {
            store.setSelectedMenu({});
          }
        }
      }
    }
  }
};
