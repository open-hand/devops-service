import React, { Fragment } from 'react';
import { Form, TextField, Select, SelectBox, UrlField, Password, EmailField, Icon, Button } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import Tips from '../../../../components/new-tips';
import { useEditAppServiceStore } from './stores';

const { Option } = Select;

const Settings = injectIntl(observer(({ record, handleTestChart }) => {
  const {
    intl: { formatMessage },
    intlPrefix,
  } = useEditAppServiceStore();

  function renderOption({ record: optionRecord, value, text }) {
    if (optionRecord.get('type') === 'DEFAULT_REPO') {
      return `${text} (默认Docker仓库)`;
    } else {
      return text;
    }
  }

  function renderInput({ value, text }) {
    if (value && value.type === 'DEFAULT_REPO') {
      return `${text} (默认Docker仓库)`;
    } else {
      return text;
    }
  }

  return (
    <div className="content-settings">
      <div className="content-settings-title">
        <Tips
          helpText={formatMessage({ id: `${intlPrefix}.setting.tips` })}
          title={formatMessage({ id: `${intlPrefix}.create.settings` })}
        />
      </div>
      <div className="content-settings-tips">
        <Icon type="info" className="content-settings-tips-icon" />
        <FormattedMessage id={`${intlPrefix}.create.settings.tips`} />
      </div>
      <Form record={record}>
        <Select
          searchable
          clearButton={false}
          name="harborRepoConfigDTO"
          optionRenderer={renderOption}
          renderer={renderInput}
          addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.docker.tips` })} />}
        />
        <SelectBox name="chartType">
          <Option value="default">
            {formatMessage({ id: `${intlPrefix}.helm.default` })}
          </Option>
          <Option value="custom">
            {formatMessage({ id: `${intlPrefix}.helm.custom` })}
          </Option>
        </SelectBox>
        {record.get('chartType') === 'custom' ? [
          <UrlField name="url" />,
          <TextField name="userName" />,
          <Password name="password" />,
        ] : null}
      </Form>
      {record.get('chartType') === 'custom' && (
        <div>
          <Button
            onClick={handleTestChart}
            funcType="raised"
            className="content-settings-button"
          >
            <FormattedMessage id={`${intlPrefix}.test`} />
          </Button>
          {record.get('chartStatus') && (
            <span>
              <Icon
                type={record.get('chartStatus') === 'success' ? 'check_circle' : 'cancel'}
                className={`content-settings-link-${record.get('chartStatus')}`}
              />
              {formatMessage({ id: `${intlPrefix}.test.${record.get('chartStatus')}` })}
            </span>
          )}
        </div>
      )}
    </div>
  );
}));

export default Settings;
