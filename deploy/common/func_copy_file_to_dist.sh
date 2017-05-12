#!/bin/bash

copy_files(){
    #创建输出jar目标目录
    dist_folder="dist"
    current_path=`pwd`

    dist_path=$current_path/$dist_folder

    rm -rf $dist_path

    echo "deploy dest folder: $dist_path"
    mkdir -p $dist_path

    cp ./deploy/post_deploy.sh $dist_path/post_deploy.sh

    #复制ensure_hostname shell到目标目录
    cp ./deploy/common/ensure_host_name.sh $dist_path/ensure_hostname.sh

    cp ./deploy/common/jvm_args.sh $dist_path/jvm_args.sh

    #进入发布模块目录
    cd ./$IA_APP

    #复制可执行jar到目标目录
    cp ./target/$IA_APP*.jar $dist_path/$IA_APP.jar

    echo "IA_APP=$IA_APP" >> $dist_path/boot.ini
    echo "source ./ensure_hostname.sh" >> $dist_path/boot.ini
    echo "ensure_env" >> $dist_path/boot.ini
    echo "source ./jvm_args.sh" >> $dist_path/boot.ini

}