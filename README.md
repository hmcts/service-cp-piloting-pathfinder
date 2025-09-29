# HMCTS API Marketplace Service Piloting Pathfinder

This repository provides an example for implementing a cross-cutting concern like 
- Logging
- JWT decryption
- Authorisation of resource endpoints.

## 🚀 Installation

To get started with this project, you'll need Java and Gradle installed.

### Prerequisites

- ☕️ **Java 21 or later**: Ensure Java is installed and available on your `PATH`.
- ⚙️ **Gradle**: You can install Gradle using your preferred method:

  **macOS (Recommended with Homebrew):**
  ```bash
  brew install gradle
  ```

  **Other Platforms:**
  Visit the [Gradle installation guide](https://gradle.org/install/) for platform-specific instructions.

You can verify installation with:
```bash
java -version
gradle -v
```

#### Add Gradle Wrapper

run `gradle wrapper`

### 🔑 Environment Setup for Local Builds

Recommended Approach for macOS Users (using `direnv`)

If you're on macOS, you can use [direnv](https://direnv.net/) to automatically load these environment variables per project:

1. Install `direnv`:
   ```bash
   brew install direnv
   ```

2. Hook it into your shell (example for bash or zsh):
   ```bash
   echo 'eval "$(direnv hook bash)"' >> ~/.bash_profile
   # or for zsh
   echo 'eval "$(direnv hook zsh)"' >> ~/.zshrc
   ```

4. Allow `direnv` to load:
   ```bash
   direnv allow
   ```

This will ensure your environment is correctly set up every time you enter the project directory.

## Static code analysis

Install PMD

```bash
brew install pmd
```
```bash
pmd check \
    --dir src/main/java \
    --rulesets \
    .github/pmd-ruleset.xml \
    --format html \
    -r build/reports/pmd/pmd-report.html
```

## Authentication

This service supports multiple authentication providers. See the authentication documentation for details:

- **[Authentication Documentation](docs/Authentication.md)** - Complete authentication guide
- **[Quick Reference](docs/QuickReference.md)** - Quick reference for developers
- **[JWTFilter Documentation](docs/JWTFilter.md)** - JWT filter specific documentation

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details