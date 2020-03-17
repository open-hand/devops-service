function domainValidator(dataSet) {
  dataSet.forEach((eachRecord) => eachRecord.getField('domain').checkValidity());
}

function handleUpdate({ value, name, record, dataSet }) {
  if (name === 'key' || name === 'domain') {
    domainValidator(dataSet);
  }
}

function handleRemove({ dataSet }) {
  domainValidator(dataSet);
}

export default ({ formatMessage }) => {
  function checkDomain(value, name, record) {
    const pa = /^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$/;
    if (value && !pa.test(value)) {
      return formatMessage({ id: 'domain.annotation.check.failed' });
    }
    if (record.get('key')) {
      const ds = record.dataSet;
      const recordKey = `${record.get('domain')}/${record.get('key')}`;
      const hasRepeat = ds.some((annotationRecord) => {
        const text = `${annotationRecord.get('domain')}/${annotationRecord.get('key')}`;
        return annotationRecord.id !== record.id && recordKey === text;
      });
      if (hasRepeat) {
        return formatMessage({ id: 'domain.annotation.check.repeat' });
      }
    }
  }

  function checkKey(value, name, record) {
    const pa = /^([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]$/;
    if (value && !pa.test(value)) {
      return formatMessage({ id: 'domain.annotation.check.failed' });
    }
    if (!value && record.get('value')) {
      return formatMessage({ id: 'mapping.keyValueSpan' });
    }
  }

  function checkValue(value, name, record) {
    if (!value && record.get('key')) {
      return formatMessage({ id: 'mapping.keyValueSpan' });
    }
  }

  return ({
    autoCreate: false,
    autoQuery: false,
    selection: false,
    paging: false,
    fields: [
      {
        name: 'domain',
        type: 'string',
        validator: checkDomain,
        maxLength: 253,
        label: formatMessage({ id: 'domain.annotation' }),
      },
      {
        name: 'key',
        type: 'string',
        validator: checkKey,
        maxLength: 63,
        label: formatMessage({ id: 'name' }),
      },
      {
        name: 'value',
        type: 'string',
        validator: checkValue,
        label: formatMessage({ id: 'value' }),
      },
    ],
    events: {
      update: handleUpdate,
      remove: handleRemove,
    },
  });
};
