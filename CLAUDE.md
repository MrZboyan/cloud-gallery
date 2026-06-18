# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 语言

始终使用中文回答。

## Overview

`cloud-gallery-parent` is a Spring Cloud microservices backend for a cloud image gallery. Stack: Java 17, Spring Boot
3.2.0, Spring Cloud 2023.0.0 + Spring Cloud Alibaba 2023.0.0.0-RC1 (Nacos), Apache Dubbo 3.3.6 for RPC, Sa-Token 1.44.0
for auth, MyBatis-Plus + MySQL, Redis/Redisson, Aliyun OSS/STS for storage.

Code comments and many identifiers are in Chinese.

## Build & Run

```bash
# Build all modules (run from repo root)
mvn clean install

# Build a single module + its dependencies
mvn -pl picture-service -am clean install

# Run a service (each has its own SpringApplication main class)
mvn -pl user-service spring-boot:run

# Run tests for one module
mvn -pl picture-service test

# Run a single test class / method
mvn -pl picture-service test -Dtest=PictureServiceImplTest
mvn -pl picture-service test -Dtest=PictureServiceImplTest#methodName
```

`common-core` and `common-web` must be installed to the local Maven repo (`mvn install`) before dependent services can
build, since they are consumed as versioned artifacts (`0.0.1-SNAPSHOT`), not reactor-only modules.

### Runtime prerequisites

- **Nacos** must be running. Config model (no `bootstrap.yml`, no `spring-cloud-starter-bootstrap` — uses
  `spring.config.import` instead):
  - Each service has a **minimal local `application.yml`** (git-untracked; holds only port, app name, Nacos connection,
    and the `spring.config.import` list). Sensitive/host values use env-var placeholders with dev defaults:
    `NACOS_SERVER_ADDR`, `NACOS_NAMESPACE`, `NACOS_USERNAME`, `NACOS_PASSWORD`, `SPRING_PROFILES_ACTIVE`.
  - Shared runtime config (mybatis-plus, knife4j; future: redis/dubbo/sa-token) lives in Nacos `common.yaml`
    (`DEFAULT_GROUP`), imported by every service. A version-controlled template is at
    [nacos-config/common.yaml](nacos-config/common.yaml).
  - Per-service config (datasource, Redis, OSS keys, Dubbo registry) lives in Nacos `${spring.application.name}-${profile}.yaml`.
  - The real `application.yml` files are intentionally untracked (they carry the Nacos namespace/credentials). They are
    NOT covered by a gitignore rule yet — avoid `git add .`, or add an explicit ignore rule.
- **MySQL** — schema is in [.sql/create_table.sql](.sql/create_table.sql) (tables: `user`, `picture`, `team`, plus
  gallery/member). Note non-standard `camelCase` column names (e.g. `userId`, `pictureId`, `isDelete`).
- **Redis** — used by Sa-Token (token storage) and Redisson (distributed locks).
- **Aliyun OSS/STS** — required by picture-service for image storage and front-end direct-upload credentials.

## Module Architecture

| Module               | Role                                                                                                                                                          |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `common/common-core` | Shared domain (no Spring web): entities, VOs, enums, `BaseResponse`/`ResultUtils`, `BusinessException`/`ErrorCode`/`ThrowUtils`, MapStruct converters, utils. |
| `common/common-web`  | Spring Boot auto-configured shared web layer. Pulled in by every business service.                                                                            |
| `dubbo-api`          | RPC contract module. `user-api` and `upload-api` hold interfaces + DTOs only; implementations live in the owning service.                                     |
| `gateway-service`    | Spring Cloud Gateway (WebFlux/reactive) — the only externally exposed entry point.                                                                            |
| `auth-service`       | Login/logout/session via Sa-Token; delegates credential checks to user-service over Dubbo.                                                                    |
| `user-service`       | User CRUD + `UserRpcService` Dubbo provider.                                                                                                                  |
| `picture-service`    | Picture CRUD/upload, OSS/STS management, `UploadRpcService` Dubbo provider.                                                                                   |

### Inter-service communication: Dubbo, not HTTP

Services call each other via Dubbo RPC, **not** REST. The pattern:

- Interface + DTOs defined in a `dubbo-api/*` module (e.g. `UserRpcService`).
- Provider: implementation annotated `@DubboService(version = "1.0.0", ...)` in the owning service (e.g.
  `UserRpcServiceImpl` in user-service, `UploadRpcServiceImpl` in picture-service).
- Consumer: field annotated `@DubboReference(version = "1.0.0")` (e.g. auth-service references `UserRpcService`). *
  *Version must match between provider and consumer.**
- Each service main class is annotated `@EnableDubbo @EnableDiscoveryClient`.
- Cross-cutting Dubbo logging/exception handling is provided by `ProviderLogAndExceptionFilter` /
  `ConsumerLogAndExceptionFilter`, registered via
  `common-web/src/main/resources/META-INF/dubbo/org.apache.dubbo.rpc.Filter`.

### Authentication flow (Sa-Token + Same-Token)

Two-layer auth model:

1. **Gateway → client (Sa-Token login check):** `SaTokenReactorFilterConfigure` intercepts `/**` and calls
   `StpUtil.checkLogin()`, excluding `/favicon.ico`, `/oauth2/**`, `/user/register`. Unauthenticated requests are
   rejected before reaching downstream services.
2. **Gateway → internal service (Same-Token):** `ForwardAuthFilter` (a `GlobalFilter`) injects a Sa-Token **Same-Token**
   header on every forwarded request. Each downstream service's `SaTokenConfigure` registers a `SaServletFilter` that
   calls `SaSameUtil.checkCurrentRequestToken()` — so services only accept calls that came through the gateway.
   `SaSameTokenRefreshTask` (scheduled) rotates the Same-Token.

Login state: `auth-service` stores the `User` object in the Sa-Token session under `UserConstant.USER_LOGIN_STATE`.
Method-level role checks use the `@AuthCheck(mustRole = ...)` annotation, enforced by `AuthInterceptor` (AOP `@Around`)
in common-web against `UserRoleEnum` (user/admin/ban/vip).

### `common-web` auto-configuration

`common-web` is a Spring Boot auto-config library, not an app. Adding the dependency auto-registers its beans via
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` → `CommonWebAutoConfiguration`, which
`@ComponentScan`s `com.common.web` and imports Redis config. This is why services get the AOP auth interceptor, global
exception handler, Sa-Token servlet filter, MyBatis-Plus config, Redis/Redisson, and Dubbo filters for free. When adding
shared web behavior, register it here so all services inherit it.

## Conventions

- **Layering (business services):** `Controller` → `Domain` (orchestration, e.g. `PictureDomain`/`PictureDomainImpl`)
  and/or `Service` (`PictureService` + MyBatis-Plus `ServiceImpl`) → `Mapper` (XML in `src/main/resources/mapper/`).
  Controllers return `BaseResponse<T>` via `ResultUtils.success(...)`.
- **DTO / Entity / VO separation:** request DTOs in each service's `model/dto`; persistence entities and response VOs in
  `common-core` (`model/<domain>/entity` and `model/<domain>/vo`). Convert entity↔VO with MapStruct converters in
  `common-core` (e.g. `UserConverter.INSTANCE.toVO(...)`).
- **Errors:** throw `BusinessException(ErrorCode.X)` or use `ThrowUtils`; never return raw error strings.
  `GlobalExceptionHandler` in common-web maps them to `BaseResponse`. Note: gateway has its own separate `ErrorCode` (
  `com.project.gateway.exception.ErrorCode`).
- **API docs:** Knife4j (OpenAPI 3) — per-service starter, aggregated at the gateway via
  `knife4j-gateway-spring-boot-starter`.
- **Mapper registration:** each service main class declares `@MapperScan("com.project.<svc>.model.mapper")`.
- **依赖与版本管理:** 所有第三方库版本由**父 pom 的 `<dependencyManagement>` + `<properties>` 版本属性**统一管理,内部模块版本用 `${revision}`。子模块声明依赖时**不要写 `<version>`**,也不要重复 `maven.compiler.*` / `sourceEncoding`(均继承自父 pom)。
- **公共依赖通过 `common-web` 传递:** `sa-token-spring-boot3-starter`、`sa-token-redis-jackson`、`knife4j`、`mysql-connector-j`、`mybatis-plus`(经 `common-core`)等由 `common-web` 统一引入,servlet 业务服务(user/auth/picture)**无需重复声明**。`gateway-service` 是 reactive 栈、**不依赖 `common-web`**,自行声明 `sa-token-reactor` 等。`lombok`/`mapstruct-processor` 为不可传递依赖(optional/provided),需各模块自行声明。
