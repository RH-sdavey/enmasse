#!/bin/bash
source test_func.sh
ENMASSE_DIR=$1
KUBEADM=$2
SYSTEMTESTS=$3
TESTCASE=$4

failure=0

setup_test

run_test || failure=$(($failure + 1))

$CURDIR/collect_logs.sh $ARTIFACTS_DIR

oc get pods
oc get pv

if [ $failure -gt 0 ]
then
    echo "Systemtests failed"
    oc get events
    exit 1
else
    teardown_test $OPENSHIFT_PROJECT
fi
