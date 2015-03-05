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
