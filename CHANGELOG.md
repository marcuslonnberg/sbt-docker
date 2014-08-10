## 0.5.1

- Fixed execution order of task `dockerBuildAndPush`

## 0.5.0

- Added new tasks to push Docker images to a registry: `dockerPush` and `dockerBuildAndPush`
- `ImageName(String)` now parses the input string

## 0.4.0

- Added an immutable Dockerfile
- Fixes related to staged files in the `add` and `copy` methods in Dockerfile
