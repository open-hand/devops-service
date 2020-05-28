import { handlePromptError } from '../../../../../../../../utils';

export default ({ title, id, formatMessage, projectId, envId, store }) => {
  const checkName = async (value, name, record) => {
    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && !pattern.test(value)) {
      return formatMessage({ id: 'network.name.check.failed' });
    } else if (value && pattern.test(value)) {
      try {
        const res = await store.checkName(projectId, envId, value);
        if (res && !res.failed) {
          return true;
        } else {
          return formatMessage({ id: 'checkNameExist' });
        }
      } catch (e) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      return true;
    }
  };

  return ({
    autoCreate: typeof id !== 'number',
    // autoQuery: typeof id === 'number',
    transport: {
      read: () => ({
        url: title === 'mapping' ? `/devops/v1/projects/${projectId}/config_maps/${id}` : `/devops/v1/projects/${projectId}/secret/${id}?to_decode=true`,
        method: 'GET',
      }),
    },
    fields: [{
      name: 'name',
      type: 'string',
      label: formatMessage({ id: 'app.name' }),
      required: true,
      validator: checkName,
      maxLength: 100,
    }, {
      name: 'description',
      type: 'string',
      label: formatMessage({ id: 'configMap.des' }),
      maxLength: 30,
    }],
  });
};
