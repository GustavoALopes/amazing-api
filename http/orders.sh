#!/usr/bin/env sh

set -eu

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <baseUrl> <tenantId>" >&2
  echo "Example: $0 http://localhost:8080 7d3e0f2a-2c87-4cf5-8d86-6a8a8dc1eb70" >&2
  exit 1
fi

base_url=${1%/}
tenant_id=$2

curl --fail-with-body --silent --show-error \
  --request POST \
  --url "${base_url}/orders/import" \
  --header "Content-Type: application/json" \
  --header "Accept: application/json" \
  --header "tenantId: ${tenant_id}" \
  --data-binary @- <<'JSON'
{
  "products": [
    {
      "skuCode": "SKU-CHAIR-001",
      "name": "Office chair",
      "price": "149.90",
      "quantity": 1
    },
    {
      "skuCode": "SKU-DESK-001",
      "name": "Standing desk",
      "price": "350.00",
      "quantity": 1
    }
  ],
  "customer": {
    "customerDocument": "123456789",
    "customerDocumentType": "NIF",
    "name": "Test Customer",
    "birthdate": "1990-05-20"
  },
  "purchasedAt": "2026-08-11T10:30:00+01:00",
  "totalValue": "499.90",
  "code": "4ef7609c-4300-49f2-9789-a199b06b912f",
  "country": "Portugal",
  "state": "Lisbon",
  "city": "Lisbon",
  "neighborn": "Alfama",
  "street": "Rua do Teste",
  "number": "42",
  "zipcode": "1100-001"
}
JSON

printf '\n'
