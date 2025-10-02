provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

locals {
  aseName = "core-compute-${var.env}"

  local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env
  local_ase = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "core-compute-aat" : "core-compute-saat" : local.aseName

  previewVaultName    = "ccpay-aat"
  nonPreviewVaultName = "ccpay-${var.env}"
  vaultName           = (var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName
  s2sUrl              = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"

  #region API gateway
  thumbprints_in_quotes     = formatlist("&quot;%s&quot;", var.bulkscanning_api_gateway_certificate_thumbprints)
  thumbprints_in_quotes_str = join(",", local.thumbprints_in_quotes)
  api_base_path             = "bulk-scanning-payment"
}

data "azurerm_key_vault" "payment_key_vault" {
  name                = local.vaultName
  resource_group_name = "ccpay-${var.env}"
}

# region API (gateway)
data "azurerm_key_vault_secret" "s2s_client_secret" {
  name         = "gateway-s2s-client-secret"
  key_vault_id = data.azurerm_key_vault.payment_key_vault.id
}

data "azurerm_key_vault_secret" "s2s_client_id" {
  name         = "gateway-s2s-client-id"
  key_vault_id = data.azurerm_key_vault.payment_key_vault.id
}

data "azurerm_key_vault_secret" "apim_app_id" {
  name         = "apim-bulk-scanning-app-id"
  key_vault_id = data.azurerm_key_vault.payment_key_vault.id
}

data "azurerm_key_vault_secret" "apim_client_id" {
  name         = "apim-bulk-scanning-client-id"
  key_vault_id = data.azurerm_key_vault.payment_key_vault.id
}

data "azurerm_key_vault_secret" "tenant_id" {
  name         = "apim-bulk-scanning-tenant-id"
  key_vault_id = data.azurerm_key_vault.payment_key_vault.id
}
