# presidio-sandbox
[MS Presidio](https://github.com/microsoft/presidio) playground

## HowTo
What to run for what purpose:
* `./gradlew build` - for building app
* `./gradlew buildDockerImage` - for building app Docker image (includes build implicitly)
* `./gradlew composeUp` - to start env with MS Presidio and app (includes Docker build implicitly)
* `./gradlew check` - run simple test against running env (includes env start implicitly)

### HowTo with docker only
* `docker run -ti --rm -u %uid% -v %current_dir%:/opt -w /opt openjdk:11 ./gradlew clean downloadPresidioProtos`
* `docker run -ti --rm -u %uid% -v %current_dir%:/opt -w /opt openjdk:11 ./gradlew assemble copyDockerResources copyDockerComposeResources`
* `docker build ./build/docker -t presidio-sandbox:1.0`
* `docker-compose -f build/docker-compose/docker-compose.yml up`

## License
<a href="http://www.wtfpl.net/"><img
       src="http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png"
       width="80" height="15" alt="WTFPL" /></a>
