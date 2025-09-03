# HMCTS API Marketplace Service SpringBoot Template

This repository provides a template for building Spring Boot applications within the HMCTS API Marketplace ecosystem. 
It includes essential configurations, dependencies, and best practices to help you get started quickly.

NOTE: This project is a template and does not contain any business logic. It is intended to be used as a reference for new HMCTS API services.

[![CI Build and Publish Increments Draft](

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

## Pact Provider Test

Run pact provider test and publish verification report to pact broker locally

Update .env file with below details (replacing placeholders with actual values):
```bash
export PACT_PROVIDER_VERSION="0.1.0-local-YOUR-INITIALS" # or any version you want to use
export PACT_VERIFIER_PUBLISH_RESULTS=true
export PACT_PROVIDER_BRANCH="ANY_BRANCH_NAME_THAT_IS_NOT_A_DEFAULT_ONE"
export PACT_BROKER_TOKEN="YOUR_PACTFLOW_BROKER_TOKEN"
export PACT_BROKER_URL="https://hmcts-dts.pactflow.io"
export PACT_ENV="local" # or value based on the environment you are testing against
```
Run Pact tests:
```bash
gradle pactVerificationTest
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details