#!/bin/bash
PROGDIR=`dirname $0`

function reset_tty {
    stty sane
}
trap reset_tty INT
for arg in "$@" ; do
    if [ "$arg" == "-trun" ]; then
	stty -echo cbreak
    fi
done

java -jar $PROGDIR/out/artifacts/checkgit_jar/checkgit.jar $PROGDIR/config "$@"
reset_tty
