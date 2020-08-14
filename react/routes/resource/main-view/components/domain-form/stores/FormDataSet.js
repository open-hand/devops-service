import { axios } from '@choerodon/boot';
import omit from 'lodash/omit';
import forEach from 'lodash/forEach';
import pick from 'lodash/pick';
import isEmpty from 'lodash/isEmpty';

function formatAnnotation(postData, data, oldAnnotations) {
  const annotations = {};
  forEach(oldAnnotations || data.annotations, ({ domain, key, value }) => {
    if (key && value) {
      annotations[`${domain ? `${domain}/` : ''}${key}`] = value;
    }
  });
  postData.annotations = isEmpty(annotations) ? null : annotations;
}

export default ({ formatMessage, intlPrefix, projectId, envId, ingressId, pathListDs, serviceDs, appServiceId, annotationDs }) => {
  async function checkName(value) {
    if (ingressId) return;
    const p = /^([a-z0-9]([-a-z0-9]?[a-z0-9])*)$/;
    if (value) {
      if (envId && p.test(value)) {
        try {
          const res = await axios.get(`/devops/v1/projects/${projectId}/ingress/check_name?env_id=${envId}&name=${value}`);
          if (res && !res.failed) {
            return true;
          } else {
            return formatMessage({ id: 'checkNameExist' });
          }
        } catch (e) {
          return formatMessage({ id: 'checkNameFailed' });
        }
      } else {
        return formatMessage({ id: 'domain.name.check.failed' });
      }
    }
  }

  function checkDomain(value) {
    const pattern = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;
    if (!pattern.test(value)) {
      return formatMessage({ id: 'domain.domain.check.failed' });
    }
  }

  function handleUpdate({ name, value, record }) {
    if (name === 'domain') {
      pathListDs.forEach((pathRecord) => pathRecord.getField('path').checkValidity());
    }

    if (name === 'isNormal') {
      if (ingressId && !value && record.get('domain') && record.get('domain') === record.getPristineValue('domain') && record.getPristineValue('certId')) {
        record.set('certId', record.getPristineValue('certId'));
      } else {
        record.get('certId') && record.set('certId', null);
      }
    }
  }

  function handleLoad({ dataSet }) {
    const record = dataSet.current;
    const annotations = [];
    record.init('isNormal', !record.get('certId'));
    forEach(record.get('annotations') || [], (value, key) => {
      const [annotationDomain, annotationKey] = key.split('/');
      const annotation = { value };
      if (annotationKey) {
        annotation.domain = annotationDomain;
        annotation.key = annotationKey;
      } else {
        annotation.key = annotationDomain;
      }
      annotations.push(annotation);
    });
    annotationDs.loadData(isEmpty(annotations) ? [{}] : annotations);
  }

  function renderLookupUrl({ record }) {
    const domain = record.get('domain');
    if (domain && !record.get('isNormal')) {
      return {
        url: `/devops/v1/projects/${projectId}/certifications/active?env_id=${envId}&domain=${domain}`,
        method: 'post',
      };
    }
  }

  return ({
    autoCreate: false,
    autoQuery: false,
    selection: false,
    paging: false,
    autoQueryAfterSubmit: false,
    children: {
      pathList: pathListDs,
      annotations: annotationDs,
    },
    transport: {
      read: {
        url: `/devops/v1/projects/${projectId}/ingress/${ingressId}`,
        method: 'get',
      },
      create: ({ data: [data] }) => {
        const postData = omit(data, '__id', '__status', 'annotations');
        formatAnnotation(postData, data);

        return ({
          url: `/devops/v1/projects/${projectId}/ingress`,
          method: 'post',
          data: postData,
        });
      },
      update: ({ data: [data] }) => {
        const postData = pick(data, 'name', 'domain', 'envId', 'appServiceId', 'certId');
        postData.domainId = ingressId;
        postData.pathList = pathListDs.toData();
        formatAnnotation(postData, data, annotationDs.toData());

        return ({
          url: `/devops/v1/projects/${projectId}/ingress/${ingressId}`,
          method: 'put',
          data: postData,
        });
      },
    },
    fields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.ingress` }), required: true, validator: checkName, maxLength: 40 },
      { name: 'domain', type: 'string', label: formatMessage({ id: `${intlPrefix}.domains` }), required: true, validator: checkDomain, maxLength: 50 },
      { name: 'isNormal', type: 'boolean', defaultValue: true, required: true, ignore: 'always' },
      {
        name: 'certId',
        type: 'string',
        textField: 'certName',
        valueField: 'id',
        label: formatMessage({ id: 'domain.form.cert' }),
        dynamicProps: {
          required: ({ record }) => !record.get('isNormal'),
          lookupAxiosConfig: renderLookupUrl,
        },
      },
      { name: 'envId', type: 'string', defaultValue: envId },
      { name: 'appServiceId', type: 'string', defaultValue: appServiceId },
    ],
    events: {
      load: handleLoad,
      update: handleUpdate,
    },
  });
};
