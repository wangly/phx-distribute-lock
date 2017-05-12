#!/bin/bash

_HOSTNAME=${HOSTNAME}

PROFILE_ENV_ONLINE=online
PROFILE_ENV_STAGING=staging
PROFILE_ENV_EP=ep
PROFILE_ENV_OFFICE=qa
PROFILE_ENV_LOCAL=local

OFFLINE_VM_HOST_SUFFIX=.office.mos
OFFLINE_SET_HOST_KEYWORD=-mos-

STAGING_HOST_KEYWORD=staging

EP_HOST_KEYWORD1=-ep
EP_HOST_KEYWORD2=-beta

LOCAL_HOST_KEYWORD=local

profile_env=none

ensure_env(){
    if  [[ ${_HOSTNAME} =~ ${OFFLINE_VM_HOST_SUFFIX}$ || ${_HOSTNAME} == *${OFFLINE_SET_HOST_KEYWORD}* ]]; then
        if [[ ${_HOSTNAME} == *${EP_HOST_KEYWORD2}* ]]; then
            profile_env=${PROFILE_ENV_EP}
        else
            profile_env=${PROFILE_ENV_OFFICE}
        fi
    else
        if  [[ ${_HOSTNAME} == *${STAGING_HOST_KEYWORD}* ]]; then
            if [[ ${_HOSTNAME} == *${EP_HOST_KEYWORD1}* ]]; then
                profile_env=${PROFILE_ENV_EP}
            else
                profile_env=${PROFILE_ENV_STAGING}
            fi
        else
            if  [[ ${_HOSTNAME} == *${LOCAL_HOST_KEYWORD}* ]]; then
                profile_env=${PROFILE_ENV_LOCAL}
            else
                profile_env=${PROFILE_ENV_ONLINE}
            fi

        fi
    fi
}
