## 1.7.0

- Support passing build arguments with `dockerBuildArguments in docker` setting.
- Add `ARG` instruction.
- Fix issue with resolving native Dockerfile path when in root of project.
- Fix issue with Docker build not passing failure properly on build failure. 

## 1.6.0

- Fix issue with building images not working with Docker 19.03.12.
- Add support for native Dockerfiles with `NativeDockerfile(filePath))`.
- Add support for Docker BuildKit.

## 1.5.0

- Add `HEALTHCHECK` instruction [#75](https://github.com/marcuslonnberg/sbt-docker/pull/75)
- The `ADD` and `COPY` instructions can now receive the `chown` flag
- Fix `dockerBuildAndPush` task in sbt 1.0

## 1.4.1

- Fix broken version check with Dockers updated version scheme [#61](https://github.com/marcuslonnberg/sbt-docker/issues/61)

## 1.4.0

- Don't use deprecated docker tag flag for versions 1.10 and up of Docker [#39](https://github.com/marcuslonnberg/sbt-docker/issues/39)

## 1.3.0

- The `dockerPush` task will now fail on Docker image push failure.
- Rename `dockerCmd` to `dockerPath`, this gives better compatibility with sbt-native-packager.

## 1.2.0

- File permissions are now kept on files that are copied with the `ADD` and `COPY` instructions [#31](https://github.com/marcuslonnberg/sbt-docker/issues/31).
- Java 7 or higher is now required.

## 1.1.0

- Support for the new `LABEL` instruction that allows adding metadata to an image [#27](https://github.com/marcuslonnberg/sbt-docker/pull/27).
- Fix a regression on caching of builds [#26](https://github.com/marcuslonnberg/sbt-docker/pull/26).

## 1.0.1

- Fix bad formatting of environment variables [#23](https://github.com/marcuslonnberg/sbt-docker/issues/23).
- Deprecate invalid `CMD` instructions.
- Change the default Docker image in `dockerAutoPackageJavaApplication` to `java:8-jre`.

## 1.0.0

- Now an Auto plugin, sbt version 0.13.5 or higher required
- An image can be tagged with multiple names
- Support for writing raw Dockerfile instructions

## 0.5.2

- Fixed execution order of task `dockerBuildAndPush`

## 0.5.0

- Added new tasks to push Docker images to a registry: `dockerPush` and `dockerBuildAndPush`
- `ImageName(String)` now parses the input string

## 0.4.0

- Added an immutable Dockerfile
- Fixes related to staged files in the `add` and `copy` methods in Dockerfile
