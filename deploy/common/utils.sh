#!/bin/bash
set -e

function cleanup() {
    echo "-----------------cleanup the dist-----------------"
    rm -rf ./doc
    rm -rf ./target
    rm -rf ./dist
}
