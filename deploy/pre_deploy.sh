#!/bin/bash
set -e

. $(dirname $0)/common/utils.sh
. $(dirname $0)/common/func_copy_file_to_dist.sh
. $(dirname $0)/common/ensure_node_version.sh

export output_doc
IA_APP=phx-distributed-lock-app

MVN_EXT_ARGS=""

cleanup

#mvn --update-snapshots package -Dmaven.test.skip=true $MVN_EXT_ARGS
mvn clean --update-snapshots test package $MVN_EXT_ARGS

copy_files

