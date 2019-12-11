import React, { useEffect, useState, useMemo } from 'react';
import { injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { Form, Select, Button } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { forEach, map, debounce, some } from 'lodash';
import useStore from './stores/useStore';

import './index.less';

const DynamicSelect = injectIntl(observer((props) => {
  const { intl: { formatMessage }, selectDataSet, optionsFilter, optionsRenderer, optionsDataSet, addText, selectName, loadMore, renderer, optionKeyName = selectName } = props;

  const selectField = selectDataSet.getField(selectName);
  const valueField = selectField.get('valueField');
  const textField = selectField.get('textField');

  const store = useStore();
  const [searchParam, setSearchParam] = useState(null);

  useEffect(() => {
    selectDataSet.getField(selectName).set('options', optionsDataSet);
    selectDataSet.create();
    loadInitData();
    return () => {
      selectDataSet.reset();
    };
  }, []);

  function handleDelete(current) {
    selectDataSet.remove(current);
  }

  function handleCreate() {
    selectDataSet.create();
  }


  async function loadData(searchPage = 1) {
    const res = await optionsDataSet.query(searchPage);
    if (res && !res.failed) {
      // 将记录以 id=>record的形式缓存起来，方便做map的快速查询
      const tempMap = {};
      forEach(optionsDataSet.data, (r) => {
        tempMap[r.get(valueField)] = r;
      });

      // 将已选中的数据拼接到现有数据 optionMap是上次的缓存数据，tempMap是本次的数据
      // 如果选中的数据已经在此次获取到的数据里，不做拼接操作
      forEach(selectDataSet.data, (r) => {
        const value = r.get(selectName);
        if (store.optionMap[value] && !tempMap[value]) {
          optionsDataSet.push(store.optionMap[value]);
        }
      });

      store.setOptionMap({
        ...store.optionMap,
        ...tempMap,
      });

      if (!res.isFirstPage && res.list && res.list.length) {
        optionsDataSet.unshift(...store.oldOptionsData);
      }
      const item = optionsDataSet.find((r) => r.get(optionKeyName) === 'More');
      item && optionsDataSet.remove(item);
      store.setOldOptionsData(optionsDataSet.records);


      // 手动加入加载更多的选项
      if (res.hasNextPage) {
        const loadMoreRecord = optionsDataSet.create({
          [valueField]: 'More',
          [textField]: 'More',
        });
        optionsDataSet.push(loadMoreRecord);
      }
      
      return res;
    } else {
      return false;
    }
  }

  function setQueryParameter(param) {
    optionsDataSet.transport.read.data = {
      params: param ? [param] : [],
      searchParam: {},
    };
  }

  function loadInitData() {
    searchParam && setQueryParameter(searchParam);
    return loadData();
  }

  const searchData = useMemo(() => debounce((value) => {
    setQueryParameter(value);
    if (value) {
      loadData();
    } else {
      loadInitData();
    }
  }, 500), []);
  
  function loadMoreWrap(e) {
    e.stopPropagation();
    if (loadMore) {
      loadMore(); 
    } else {
      loadData(optionsDataSet.currentPage + 1);
    }
  }

  function handleInput(e) {
    e.persist();
    setSearchParam(e.target.value);
    searchData(e.target.value);
  }

  function handleBlur() {
    optionsDataSet.transport.read.data = null;
    loadData();
  }

  function optionRendererWraper({ record, text, value }) {
    if (text === value && value === 'More') {
      return <div
        onClick={loadMoreWrap.bind(this)}
        className="c7n-option-popover"
      >
        <span className="c7n-option-span">{formatMessage({ id: 'loadMore' })}</span>
      </div>;
    }
    if (!renderer) return text;
    return optionsRenderer({ record, text, value });
  }

  function rendererWraper({ record, text, value }) {
    if (!value || !store.optionMap[value]) return;
    if (!renderer) return text;
    return <div className="renderer-wraper">{renderer({ valueRecord: record, text, value, optionRecord: store.optionMap[value] })}</div>;
  }


  function optionsFilterWaper(record) {
    if (record.get(valueField) === 'More') return true;
    if (optionsFilter) return optionsFilter(record);
    const flag = some(selectDataSet.created, (r) => r.get(selectName) === record.get(optionKeyName));
    return !flag;
  }

  return (<div className="dynamic-select-form">
    {map(selectDataSet.created, (createdRecord, index) => (
      <div className="dynamic-select-form-item" key={`dynamic-select-form-${index}`}>
        <Form record={createdRecord}>
          <Select name={selectName} optionRenderer={optionRendererWraper} renderer={rendererWraper} optionsFilter={optionsFilterWaper} searchable onInput={handleInput} searchMatcher={() => true} onBlur={handleBlur.bind(this, createdRecord)} />
        </Form>
        <Button
          icon="delete"
          shape="circle"
          onClick={() => handleDelete(createdRecord)}
          disabled={selectDataSet.created.length === 1}
          className="dynamic-select-form-button"
        />
      </div>
    ))}
    <Button
      icon="add"
      color="primary"
      onClick={handleCreate}
    >
      {addText}
    </Button>
  </div>);
}));

DynamicSelect.propTypes = {
  selectDataSet: PropTypes.object.isRequired,
  optionsDataSet: PropTypes.object.isRequired,
  addText: PropTypes.string.isRequired,
  selectName: PropTypes.string.isRequired,
  optionKeyName: PropTypes.string,
  optionsRenderer: PropTypes.func,
  optionsFilter: PropTypes.func,
  loadMore: PropTypes.func,
  renderer: PropTypes.func,
};
export default DynamicSelect;
