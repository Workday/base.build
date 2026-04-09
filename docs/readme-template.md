# {Project Name}
{One-line description}

[![CI](https://github.com/Workday/{repo}/actions/workflows/main-pull-request.yml/badge.svg)](https://github.com/Workday/{repo}/actions/workflows/main-pull-request.yml)
[![Maven Central](https://img.shields.io/maven-central/v/{groupId}/{rootArtifact})](https://central.sonatype.com/artifact/{groupId}/{rootArtifact})
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Overview

{2–3 sentences: what it does, why it exists, primary use case}

## Modules

| Module | Purpose |
|--------|---------|
| `{module}` | {purpose} |

## Requirements

- Java {version}+
- Maven (wrapper included — no separate install needed)

{Add any project-specific requirements here}

## Using this Library

Add individual modules as dependencies. All modules share the same version:

```xml
<dependency>
    <groupId>{groupId}</groupId>
    <artifactId>{module}</artifactId>
    <version>VERSION</version>
</dependency>
```

Replace `VERSION` with the latest version shown in the Maven Central badge above.

## Building from Source

```bash
./mvnw clean install
```

To build a custom version:

```bash
./mvnw -Drevision=x.y.z-SNAPSHOT-my-name clean install
```

{Add any project-specific build notes here}

## Contributing

Code style is enforced by Checkstyle: no tabs, no star imports, final locals and parameters, braces
required on all blocks, no `assert` statements. Import order: third-party, standard Java, then
static. IntelliJ configuration is at `config/intellij/CodeStyle.xml`.

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/).

## License

Apache 2.0 — see [LICENSE](LICENSE)
