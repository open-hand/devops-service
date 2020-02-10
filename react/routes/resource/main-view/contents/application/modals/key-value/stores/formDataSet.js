import { handlePromptError } from '../../../../../../../../utils';

export default ({ id, formatMessage, projectId, envId, store }) => {
  const checkName = (value, name, record) => {
    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && !pattern.test(value)) {
      return formatMessage({ id: 'network.name.check.failed' });
    } else if (value && pattern.test(value)) {
      store.checkName(projectId, envId, value)
        .then(res => {
          if (handlePromptError(res, false)) {
            return true;
          } else {
            return formatMessage({ id: 'checkNameExist' });
          }
        })
        .catch(error => formatMessage({ id: 'checkNameFailed' }));
    } else {
      return true;
    }
  };

  return ({
    autoQuery: typeof id === 'number',
    transport: {
      read: () => ({
        url: `/devops/v1/projects/${projectId}/config_maps/${id}`,
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
