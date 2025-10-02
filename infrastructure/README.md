# Terraform to create Azure Apim product and underlying apis

Colin has reached out to chat with Dave Jones who seems to be the person setting up the tf for apim in ccpay-bulkscanning-app

The key files are detailed here


# Template files
template/api.json - 163 lines seems to be the bulk of the config
Defines the following parameters
apiManagementServiceName
apiName
apiProductName
serviceUrl
apiBasePath
policy

Sets the following parameters
"displayName": "bulk-scanning payments API", // line80

"urlTemplate": "/bulk-scan-payment", // line99



template/cft-api-policy.xml
expects X-ARR-ClientCertThumbprint header
Sets var client_id to cpay-s2s-client-id
Sets var client_secret to ccpay-s2s-client-secret
Sets var one_time_password to a hashed secret
Sets var s2sBearertoken which is set by request to ${s2s_base_url}/lease
Sets header ServiceAuthorization to response from s2sBearertoken


template/cft-api-policy-oauth2.xml
... almost the same as cft-api-policy except it does not assert X-ARR-ClientCertThumbprint and deletes Authorization header


# Var Files
Contain certificate thumbprints for target environment
i.e. bulkscanning_api_gateway_certificate_thumbprints = ["7744A2F56BD3B73C0D7FED61309E1C65AF08538C",...]

Contain aks subscription id
i.e. aks_subscription_id = "8a07fdcd-6abd-48b3-ad88-ff737a4b9e3c"

Contain apim environment suffix
i.e. apim_suffix="test"

aat.tfvars
apim_suffix = "stg"

demo.tfvars
additional_databases = ["postgresql-db2"]

perftest.tfvars
apim_suffix = "test"

preview.tfvars
(empty file)

prod.tfvars
( no apim_suffix )


# Terraform files 

cft-api-mgmt-oauth2-logging.tf


cft-api-mgmt-oauth2.tf


cft-api-mgmt-subscriptions.tf


cft-api-mgmt.tf


main.tf
Default entry point for applying terraform


output.tf


provider.tf


sdp.tf


state.tf


template


variables.tf


versions.tf