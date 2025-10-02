# Terraform to create Azure Apim product and underlying apis

The terraform uses a hmcts cnp tf module
https://github.com/hmcts/cnp-module-api-mgmt-product
... A terraform module for creating a product in API mgmt
... 9 inputs including api_mgmt_name, api_mgmt_rg, approval_required, name, published, 
    subscription_required, subscriptions_limit, product_access_control_groups, product_policy )
... output of product_id


And hmcts cnp tf module
https://github.com/hmcts/cnp-module-api-mgmt-api


And hmcts cnp tf module
https://github.com/hmcts/cnp-module-api-mgmt-api-policy
... A terraform module for creating a policy in API mgmt
... 4 inputs api_mgmt_name, api_mgmt_rg, api_name, api_policy_xml_content


It also references swagger_url
https://raw.githubusercontent.com/hmcts/cnp-api-docs/master/docs/specs/ccpay-payment-app.bulk-scanning.json

And cnp-api specs
https://github.com/hmcts/cnp-api-docs/tree/master/docs/specs


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
There are the following object types in the terraform
provider
variable
locals
data
module
resource


cft-api-mgmt-subscriptions.tf - 33 lines
Defines azurerm_api_management_subscription and azurerm_key_vault_secret resources
( Dropped exela )


cft-api-mgmt.tf - 57 lines
Refers to hmcts modules and templates


main.tf
Default entry point for applying terraform


output.tf - 7 lines
output "dummy_value"
output "s2s_url"

provider.tf - 12 lines
dunno !

sdp.tf - 65 lines
Dropped this ... its service-desk-plus with postgres database


state.tf - 14 lines
standard required_providers


variables.tf - 82 lines
product = ccpay
component = bulkscanning-api
product_name = bulk-scanning-payment
location_app = UK South
bulkscanning_api_gateway_certificate_thumbprints []
jenkins_AAD_objectId =(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. 
The object ID must be unique for the list of access policies.
aks_subscription_id
apim_suffix
various database and postgres settings


versions.tf - 3 lines
standard required_version