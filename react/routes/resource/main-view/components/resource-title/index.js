import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { runInAction } from 'mobx';
import { Icon, Tooltip } from 'choerodon-ui';
import PageTitle from '../../../../../components/page-title';
import { useResourceStore } from '../../../stores';

import './index.less';

function ResourceTitle(props) {
  const { record, iconType, statusKey, errorKey } = props;
  const {
    resourceStore,
    treeDs,
    resourceStore: { getSelectedMenu: { key } },
  } = useResourceStore();

  function getCurrent() {
    if (record) {
      const id = record.get('id');
      const name = record.get('name');
      const status = record.get(statusKey);
      const errorText = record.get(errorKey);

      return {
        id,
        name,
        status,
        errorText,
      };
    }
    return null;
  }

  function getTitle() {
    const current = getCurrent();
    if (current) {
      const { name, status, errorText } = current;
      return <div className="c7ncd-resource-title">
        <Icon type={iconType} className="c7ncd-resource-title-icon" />
        <span className="c7ncd-resource-title-name">{name}</span>
        {status === 'failed' && (
          <Tooltip title={errorText || ''}>
            <Icon type="error" className="c7ncd-resource-title-error-icon" />
          </Tooltip>
        )}
      </div>;
    }
    return null;
  }

  function getFallBack() {
    const { name, status } = resourceStore.getSelectedMenu;
    return <div className="c7ncd-resource-title">
      <Icon type={iconType} className="c7ncd-resource-title-icon" />
      <span>{name}</span>
      {status === 'failed' && (
        <Icon type="error" className="c7ncd-resource-title-error-icon" />
      )}
    </div>;
  }

  useEffect(() => {
    const current = getCurrent();
    if (current) {
      const { id, name, status } = current;
      const menuItem = treeDs.find((item) => item.get('key') === key && item.get('id') === id);
      if (menuItem && (menuItem.get('name') !== name || menuItem.get('status') !== status)) {
        runInAction(() => {
          menuItem.set({ name, status });
          resourceStore.setSelectedMenu({
            ...resourceStore.getSelectedMenu,
            ...current,
          });
        });
      }
    }
  }, [record]);

  return (
    <PageTitle content={getTitle()} fallback={getFallBack()} />
  );
}

ResourceTitle.propTypes = {
  iconType: PropTypes.string.isRequired,
  statusKey: PropTypes.string,
  errorKey: PropTypes.string,
};

ResourceTitle.defaultProps = {
  statusKey: 'status',
  errorKey: 'error',
};

export default observer(ResourceTitle);
