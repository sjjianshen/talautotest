case $1 in
  core)
    gradle autotest-core:fatJar autotest-core:install
    ;;
  gradle)
    gradle autotest-gradle-plugin:Jar autotest-gradle-plugin:install
    ;;
  maven)
    cd autotest-maven-plugin && mvn install && cd ..
    ;;
  *)
    echo "no goal match"
    ;;
esac
