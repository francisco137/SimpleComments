#!/bin/bash

PG_VER=11
PG_HOST=localhost
PG_PORT=5432
APP_PORT=9000
PG_BASE=francisco_comments
PG_USER=francisco_comments
PG_PASS="ostatnie haslo testowe"

con_name="francisco_comments"
img_name="francisco_comments"
dir_name="francisco_comments"

vol_comm="francisco_comments"
vol_root="francisco_root"
vol_post="francisco_postgres"
vol_home="francisco_home"

vol_c="$vol_comm:/$dir_name"
vol_r="$vol_root:/root"
    vol_p="$vol_post:/var/lib/postgresql/$PG_VER/main"
vol_h="$vol_home:/home/$PG_USER"

if [ "$1" = "--init" ]; then

    if [[ "$2" == "with_comm" || "$3" == "with_comm" || "$4" == "with_comm" || "$5" == "with_comm" ]]; then docker volume rm $vol_comm; fi
    if [[ "$2" == "with_root" || "$3" == "with_root" || "$4" == "with_root" || "$5" == "with_root" ]]; then docker volume rm $vol_root; fi
    if [[ "$2" == "with_post" || "$3" == "with_post" || "$4" == "with_post" || "$5" == "with_post" ]]; then docker volume rm $vol_post; fi
    if [[ "$2" == "with_home" || "$3" == "with_home" || "$4" == "with_home" || "$5" == "with_home" ]]; then docker volume rm $vol_home; fi

    docker build --build-arg DIR_NAME=$dir_name --build-arg PG_VER=$PG_VER -t $img_name .

    docker run -it --rm --name $con_name -v $vol_c -v $vol_r -v $vol_p -v $vol_h -p $APP_PORT:$APP_PORT $img_name /bin/bash /$dir_name/app_init.sh "$PG_VER" "$PG_HOST" "$PG_PORT" "$PG_BASE" "$PG_USER" "$PG_PASS" "$dir_name"
    docker run -it --rm --name $con_name -v $vol_c -v $vol_r -v $vol_p -v $vol_h -p $APP_PORT:$APP_PORT $img_name /bin/bash /$dir_name/app_test.sh "$PG_VER"
fi

if [ "$1" = "--test" ]; then
    docker run -it --rm --name $con_name -v $vol_c -v $vol_r -v $vol_p -v $vol_h -p $APP_PORT:$APP_PORT $img_name /bin/bash /$dir_name/app_test.sh "$PG_VER"
fi

if [ "$1" = "--run" ]; then
    docker run  -d --rm --name $con_name -v $vol_c -v $vol_r -v $vol_p -v $vol_h -p $APP_PORT:$APP_PORT $img_name /bin/bash /$dir_name/app_run.sh "$PG_VER"
    echo -e "\nSimple comments application should be available at http://localhost:9000/ soon.\n"
fi

if [ "$1" = "--stop" ]; then
    container=`docker ps -aqf "name=$con_name"`
    docker stop $container
    docker rm   $container 2&>/dev/null
fi

if [ "$1" = "--bash" ]; then
    docker run -it --rm --name $con_name -v $vol_c -v $vol_r -v $vol_p -v $vol_h -p $APP_PORT:$APP_PORT $img_name
fi

if [ "$1" = "--remove" ]; then

    container=`docker ps -aqf "name=$con_name"`
    docker stop $container
    docker rm   $container 2&>/dev/null

    docker volume rm $vol_comm
    docker volume rm $vol_root
    docker volume rm $vol_post
    docker volume rm $vol_home

    docker image  rm $img_name
fi
