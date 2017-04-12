hadoop fs -test -d /user/root/faces
  if [ $? == 0 ]
    then
      hadoop fs -rm -r -f /user/root/faces
  fi
hadoop jar detectFaces.jar images.hib faces
