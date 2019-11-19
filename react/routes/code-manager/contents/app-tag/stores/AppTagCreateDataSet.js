import _ from 'lodash';

export default (formatMessage, projectId, appServiceId, AppTagStore) => {
  const checkTagName = (value, name, revord) => {
    const pa = /^\d+(\.\d+){2}$/;
    const SemanticVersion = /^\d+(\.\d+){2}-[a-zA-Z0-9]+(\.[a-zA-Z0-9]+)*$/;
    if (value && (pa.test(value) || SemanticVersion.test(value))) {
      AppTagStore.checkTagName(projectId, value, appServiceId)
        .then((data) => {
          if (!data) {
            return formatMessage({ id: 'apptag.checkName' });
          }
        });
    } else {
      return formatMessage({ id: 'apptag.checkNameReg' });
    }
  };

  return {
    autoCreate: true,
    fields: [
      { name: 'tag', type: 'string', label: formatMessage({ id: 'apptag.name' }), required: true, validator: checkTagName },
      { name: 'ref', type: 'string', label: formatMessage({ id: 'apptag.ref' }), required: true },
      { name: 'release', type: 'string' },
    ],
    transport: {
      create: ({ data: [data] }) => {
        const { tag, ref, release } = data;
        return ({
          url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/tags?tag=${tag}&ref=${ref}`,
          method: 'post',
          data: release,
        });
      },
    },
  };
};
