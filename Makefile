SHELL=bash

JAVA_OPTS=-Xmx1024m -Xms1024m -Xdebug -Xrunjdwp:transport=dt_socket,address=8004,server=y,suspend=n

## The default website content directory
WEBSITE_DEFAULT:=target/website

ifndef WEBSITE
$(warning WEBSITE env var not found applying default: $(WEBSITE_DEFAULT))
export WEBSITE = ${WEBSITE_DEFAULT}
endif

## The default length of time before a transaction is archived is 1440h (60 days)
ARCHIVING_TRANSACTIONS_THRESHOLD_DEFAULT:=1440h

ifndef ARCHIVING_TRANSACTIONS_THRESHOLD
$(warning ARCHIVING_TRANSACTIONS_THRESHOLD env var not found applying default: ${ARCHIVING_TRANSACTIONS_THRESHOLD_DEFAULT})
export ARCHIVING_TRANSACTIONS_THRESHOLD = ${ARCHIVING_TRANSACTIONS_THRESHOLD_DEFAULT}
endif

## The default path should be archived-transactions within zebedee default
ARCHIVING_TRANSACTIONS_PATH_DEFAULT:=$zebedee_root/archived-transactions

ifndef ARCHIVING_TRANSACTIONS_PATH
$(warning ARCHIVING_TRANSACTIONS_PATH env var not found applying default: ${ARCHIVING_TRANSACTIONS_PATH_DEFAULT})
export ARCHIVING_TRANSACTIONS_PATH = ${ARCHIVING_TRANSACTIONS_PATH_DEFAULT}
endif

## The default slack username
SLACK_USER_NAME_DEFAULT:=Zebedee-test

ifndef SLACK_USER_NAME
$(warning SLACK_USER_NAME env var not found applying default: ${SLACK_USER_NAME_DEFAULT})
export SLACK_USER_NAME = ${SLACK_USER_NAME_DEFAULT}
endif

## The default slack channel
SLACK_CHANNEL_DEFAULT:=slack-client-test

ifndef SLACK_CHANNEL
$(warning SLACK_CHANNEL env var not found applying default: ${SLACK_CHANNEL_DEFAULT})
export SLACK_CHANNEL = ${SLACK_CHANNEL_DEFAULT}
endif

## The default publish transactions directory
TRANSACTIONS_DEFAULT:=target/transactions

ifndef TRANSACTION_STORE
$(warning TRANSACTION_STORE env var not found applying default: ${TRANSACTIONS_DEFAULT})
export TRANSACTION_STORE = ${TRANSACTIONS_DEFAULT}
endif

## The default bind address
PORT_DEFAULT:=8084

ifndef PORT
$(warning PORT env var not found applying default: $(PORT_DEFAULT))
export PORT = ${PORT_DEFAULT}
endif

## The default thread pool size
PUBLISHING_THREAD_POOL_SIZE_DEFAULT:=100

ifndef PUBLISHING_THREAD_POOL_SIZE
$(warning PUBLISHING_THREAD_POOL_SIZE env var not found applying default: ${PUBLISHING_THREAD_POOL_SIZE_DEFAULT})
export PUBLISHING_THREAD_POOL_SIZE = ${PUBLISHING_THREAD_POOL_SIZE_DEFAULT}
endif

## The default max file upload size in bytes (-1 == unlimited)
MAX_FILE_UPLOAD_SIZE_MB_DEFAULT:=-1

ifndef MAX_FILE_UPLOAD_SIZE_MB
$(warning MAX_FILE_UPLOAD_SIZE_MB env var not found applying default: ${MAX_FILE_UPLOAD_SIZE_MB_DEFAULT})
export MAX_FILE_UPLOAD_SIZE_MB = ${MAX_FILE_UPLOAD_SIZE_MB_DEFAULT}
endif

## The default max request size in bytes (-1 == unlimited)
MAX_REQUEST_SIZE_MB_DEFAULT:=-1

ifndef MAX_REQUEST_SIZE_MB
$(warning MAX_REQUEST_SIZE_MB env var not found applying default: ${MAX_REQUEST_SIZE_MB_DEFAULT})
export MAX_REQUEST_SIZE_MB = ${MAX_REQUEST_SIZE_MB_DEFAULT}
endif

## The file upload threshold - files uploading this size will be written to a temp file rather than being held in memory. Size in MB
FILE_THRESHOLD_SIZE_MB_DEFAULT:=10

ifndef FILE_THRESHOLD_SIZE_MB
$(warning FILE_THRESHOLD_SIZE_MB env var not found applying default: ${FILE_THRESHOLD_SIZE_MB_DEFAULT})
export FILE_THRESHOLD_SIZE_MB = ${FILE_THRESHOLD_SIZE_MB_DEFAULT}
endif

test:
	mvn -Dossindex.skip test
audit:
	mvn ossindex:audit
lint:
	exit #no linting action defined
ensure_dirs:
	@if [[ $(WEBSITE) == $(WEBSITE_DEFAULT) ]]; then mkdir -p $(WEBSITE_DEFAULT); fi
	@if [[ $(TRANSACTION_STORE) == $(TRANSACTIONS_DEFAULT) ]]; then mkdir -p $(TRANSACTIONS_DEFAULT); fi
build:
	mvn -DskipTests -Dossindex.skip clean package
debug: build ensure_dirs
	 java ${JAVA_OPTS} -jar target/the-train-*.jar
.PHONY: build debug test audit ensure_dirs

