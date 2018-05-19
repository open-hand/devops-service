{{/* vim: set filetype=mustache: */}}
{{- /*
service.labels.standard prints the standard service Helm labels.
The standard labels are frequently used in metadata.
*/ -}}
{{- define "service.labels.standard" -}}
choerodon.io/release: {{ .Release.Name | quote }}
choerodon.io/application: {{ .Chart.Name | quote }}
choerodon.io/version: {{ .Chart.Version | quote }}
choerodon.io/service: {{ .Chart.Name | quote }}
choerodon.io/metrics-port: {{ .Values.deployment.managementPort | quote }}
{{- end -}}

{{- define "service.match.labels" -}}
choerodon.io/release: {{ .Release.Name | quote }}
{{- end -}}
{{- define "service.annotations.standard" -}}
{{- if not (empty .Values.metrics.label ) -}}
choerodon.io/metrics-label: {{ .Values.metrics.label | quote }}
{{ end }}
{{- if not (empty .Values.metrics.path ) -}}
choerodon.io/metrics-path: {{ .Values.metrics.path | quote }}
{{- end }}
choerodon.io/logs-parser: {{ .Values.logs.parser | default "none" | quote }}
{{- end -}}