#!/bin/bash

private_key="keys/server_private_key_$(hostname).pem"
public_key="keys/server_public_key_$(hostname).pem"

# Generate private key (Ed25519 is EdDSA with Curve25519)
openssl genpkey -algorithm ED25519 -out "$private_key"

# Extract the public key
openssl pkey -in "$private_key" -pubout -out "$public_key"

# Copy the public key to the peers directory.
# .../m2m/server/src/resources/keys/public_key -> .../m2m/peer/src/resources/keys/public_key
# In order to properly work, this script must run in the server resources directory
cp "$public_key" ../../../peer/src/resources/keys/