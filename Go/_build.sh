#!/bin/bash

# Usage:
#    _build -r [run args --] [go install args]
# or
#    _build test [go test args]

RUN=""
RUN_ARGS=""
CMD_ARGS=""
VERB="install"
PKGS="miniyaml"
EXEC="miniyaml"
if [[ "$1" == "-r" ]]; then
    shift
    RUN="1"
    while [[ -n "$1" ]]; do
        if [[ "$1" == "--" ]]; then
            shift
            break
        else
            RUN_ARGS="$RUN_ARGS $1"
            shift
        fi
    done
elif [[ "$1" == "test" ]]; then
    shift
    VERB="test"
    if [[ "$1" == "-i" ]]; then
        CMD_ARGS="-i"
        shift
    fi
    if [[ -z "$1" ]]; then
        PKGS=$(find src -name "*_test.go" | sed -e 's@src/@@' -e 's@/[a-z0-9_-]\+_test.go$@@' | sort | uniq)
    else
        PKGS=""
    fi
fi

UNAME=$(uname)

if [[ ${UNAME:0:6} == "CYGWIN" ]]; then
	export GOPATH=$(cygpath -w "$PWD")
else
	export GOPATH="$PWD"
fi

echo go $VERB -v $CMD_ARGS $* $PKGS
go $VERB -v $CMD_ARGS $* $PKGS

if [[ $? != 0 ]]; then
    echo "# Failed"
else
    echo "# OK"
    ls -la pkg/*/$EXEC*.a
    if [[ -n $RUN ]]; then
        bin/$EXEC $RUN_ARGS
    fi
fi

