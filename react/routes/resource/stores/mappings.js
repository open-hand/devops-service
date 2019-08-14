export const itemTypeMappings = {
  ENV_ITEM: 'environment',
  APP_ITEM: 'application',
  IST_ITEM: 'instances',
  SERVICES_ITEM: 'services',
  INGRESS_ITEM: 'ingresses',
  CERT_ITEM: 'certifications',
  MAP_ITEM: 'configMaps',
  CIPHER_ITEM: 'secrets',
  CUSTOM_ITEM: 'customResources',
  SERVICES_GROUP: 'group_services',
  INGRESS_GROUP: 'group_ingresses',
  CERT_GROUP: 'group_certifications',
  MAP_GROUP: 'group_configMaps',
  CIPHER_GROUP: 'group_secrets',
  CUSTOM_GROUP: 'group_customResources',
  IST_GROUP: 'group_instances',
};

export const viewTypeMappings = {
  IST_VIEW_TYPE: 'instance',
  RES_VIEW_TYPE: 'resource',
};

export const RES_TYPES = ['instances', 'services', 'ingresses', 'certifications', 'configMaps', 'secrets', 'customResources'];

export const ENV_KEYS = ['id', 'name', 'connect', 'synchronize'];
