{{/* vim: set filetype=mustache: */}}

{{/*
Return the proper image name
*/}}
{{- define "server.image" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.image "chart" .Chart "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper skywalking image name
*/}}
{{- define "skywalking.image" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.skywalking "chart" .Chart "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper java base image name
*/}}
{{- define "initDatabases.image" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.initDatabases "chart" .Chart "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper java base image name
*/}}
{{- define "base.image" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.base "chart" .Chart "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper Docker Image Registry Secret Names
*/}}
{{- define "server.imagePullSecrets" -}}
{{- include "common.images.pullSecrets" (dict "images" (list .Values.initDatabases .Values.image .Values.base .Values.skywalking) "global" .Values.global) -}}
{{- end -}}

{{/*
Get the workingDir.
*/}}
{{- define "server.workingDir" -}}
{{- printf "%s" (default .Values.persistence.mountPath .Values.workingDir) -}}
{{- end -}}

{{/*
Get the credentials secret.
*/}}
{{- define "server.secretName" -}}
{{- if .Values.global.server.existingSecret }}
    {{- printf "%s" .Values.global.server.existingSecret -}}
{{- else if .Values.existingSecret -}}
    {{- printf "%s" .Values.existingSecret -}}
{{- else -}}
    {{- printf "%s" (include "common.names.fullname" .) -}}
{{- end -}}
{{- end -}}

{{/*
Return true if a PVC object should be created
*/}}
{{- define "server.createPVC" -}}
{{- if and .Values.persistence.enabled (not .Values.persistence.existingClaim) }}
    {{- true -}}
{{- end -}}
{{- end -}}

{{/*
Return the PVC name
*/}}
{{- define "server.claimName" -}}
{{- if and .Values.persistence.existingClaim }}
    {{- printf "%s" (tpl .Values.persistence.existingClaim $) -}}
{{- else -}}
    {{- printf "%s" (include "common.names.fullname" .) -}}
{{- end -}}
{{- end -}}

{{/*
Returns the proper service account name depending if an explicit service account name is set
in the values file. If the name is not set it will default to either common.names.fullname if serviceAccount.create
is true or default otherwise.
*/}}
{{- define "server.serviceAccountName" -}}
    {{- if .Values.serviceAccount.create -}}
        {{ default (include "common.names.fullname" .) .Values.serviceAccount.name }}
    {{- else -}}
        {{ default "default" .Values.serviceAccount.name }}
    {{- end -}}
{{- end -}}
