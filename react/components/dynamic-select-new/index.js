import React, { Fragment, useCallback, useMemo, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Form, Select, Button } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';

import './index.less';

const DynamicSelect = observer((props) => {
  const { selectDataSet, optionsFilter, optionsRenderer, addText, selectName } = props;

  useEffect(() => {
    selectDataSet.create();
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
  return (<div className="dynamic-select-form">
    {map(selectDataSet.created, (createdRecord, index) => (
      <div className="dynamic-select-form-item" key={`dynamic-select-form-${index}`}>
        <Form record={createdRecord}>
          <Select name={selectName} optionsFilter={optionsFilter} searchable optionRenderer={optionsRenderer} />
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
});

DynamicSelect.propTypes = {
  selectDataSet: PropTypes.object.isRequired,
  optionsFilter: PropTypes.func.isRequired,
  optionsRenderer: PropTypes.func.isRequired,
  addText: PropTypes.string.isRequired,
  selectName: PropTypes.string.isRequired,
};

export default DynamicSelect;
