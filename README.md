# Universal Resolver Driver: DID BSV

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:bsv** identifiers.

# Basic Diagram of usage
![img.png](img.png)

## Specifications

* [DID BSV Method Specification](https://bsvblockchain.org/did-method-specification)

## Example DIDs
* Valid did = did:bsv:tba
* Valid did = did:bsv:tba
* Deactivated did = did:bsv:tba

## Build and Run (Docker)

1. Build the Docker image:
```bash
docker build -f docker/Dockerfile.jvm -t bsvdid-driver .

docker run --network did-network \
  -p 9115:9115 \
  -e BSV_RESOLVER_URL="http://host.docker.internal:9111/" \
  -e QUARKUS_LOG_CONSOLE_JSON="true" \
  -e QUARKUS_LOG_LEVEL="INFO" \
  -e QUARKUS_LOG_CATEGORY__ORG_BSV__LEVEL="DEBUG" \
  --name bsvdid-driver \
  bsvdid-driver

curl -X GET http://localhost:9115/1.0/identifiers/did:bsv:address:1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa
```  

## Driver Environment Variables
The driver recognizes the following environment variables:

### `bsv_resolver_url`
* Specifies path to bsv resolver.
* Default value: https://bsvdid-universal-resolver.nchain.systems

## Driver Metadata
The driver returns the following metadata in addition to a DID document:
* `x-httpStatus`: In `didResolutionMetadata` we add custom properties which represent returned status code from `bsv resolver`.