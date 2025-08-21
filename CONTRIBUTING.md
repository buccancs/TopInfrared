# Contributing to TopInfrared

Welcome to TopInfrared! We appreciate your interest in contributing to this thermal imaging application project. This guide will help you get started with contributing effectively.

## 📋 Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 8 or JDK 11
- Android SDK API Level 34
- NDK Version 21.3.6528147
- Git knowledge
- Basic understanding of Android development

### First Time Setup

1. **Fork and Clone**
   ```bash
   # Fork the repository on GitHub
   git clone https://github.com/YOUR-USERNAME/TopInfrared.git
   cd TopInfrared
   git remote add upstream https://github.com/buccancs/TopInfrared.git
   ```

2. **Set up Development Environment**
   ```bash
   chmod +x gradlew
   ./gradlew tasks # Verify setup
   ```

3. **Create Development Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## 🔧 Development Guidelines

### Code Style

#### Kotlin Code Style
- Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use 4 spaces for indentation
- Line length: 120 characters maximum
- Use meaningful variable and function names

#### Java Code Style (Legacy Code)
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Maintain consistency with existing codebase
- 4 spaces for indentation

#### Example:
```kotlin
// Good
class ThermalImageProcessor {
    private val temperatureRange: ClosedFloatingPointRange<Float> = -40f..120f
    
    fun processImage(inputImage: Bitmap): ThermalImage {
        // Implementation
    }
}

// Avoid
class thermal_processor {
    var temp_range = (-40f..120f)
    fun processImg(img: Bitmap): ThermalImage { /*...*/ }
}
```

### Commit Message Convention

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

#### Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or modifying tests
- `chore`: Maintenance tasks

#### Examples:
```bash
feat(thermal): add temperature calibration algorithm
fix(ble): resolve connection timeout on Samsung devices
docs(readme): update installation instructions
refactor(ui): modernize thermal display components
test(processor): add unit tests for image processing
```

## 🧪 Testing Requirements

### Unit Tests
- Write unit tests for all new functionality
- Maintain minimum 70% code coverage for new code
- Test files should be in `src/test/java/`

### Integration Tests
- Add integration tests for Bluetooth functionality
- Test hardware communication protocols
- Integration tests in `src/androidTest/java/`

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📁 Project Structure Guidelines

### Module Organization
- **Feature modules**: Keep related functionality together
- **Library modules**: Reusable components across features
- **Clean separation**: Avoid circular dependencies

### File Organization
```
feature-module/
├── src/main/java/
│   ├── ui/           # UI components
│   ├── data/         # Data layer
│   ├── domain/       # Business logic
│   └── di/           # Dependency injection
├── src/test/         # Unit tests
└── src/androidTest/  # Integration tests
```

## 🎯 Types of Contributions

### 1. Bug Fixes
- Check existing issues before starting
- Provide detailed reproduction steps
- Include relevant device/OS information
- Test fix on multiple Android versions

### 2. New Features
- Discuss major features in issues first
- Follow existing architectural patterns
- Update documentation
- Add comprehensive tests

### 3. Performance Improvements
- Benchmark before and after changes
- Consider battery life impact
- Test on various device configurations
- Document performance implications

### 4. Documentation
- Keep README up to date
- Add inline code documentation
- Update API documentation
- Include examples where helpful

### 5. Hardware Support
- Test with actual hardware devices
- Document hardware requirements
- Provide device-specific configurations
- Consider backward compatibility

## 🔄 Pull Request Process

### Before Submitting
1. **Update your branch**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run quality checks**
   ```bash
   ./gradlew clean build
   ./gradlew test
   ./gradlew lint
   ```

3. **Test thoroughly**
   - Manual testing on target devices
   - Automated test suite passes
   - Performance impact assessment

### PR Requirements
- [ ] Clear description of changes
- [ ] Links to related issues
- [ ] Screenshots/videos for UI changes
- [ ] Unit tests added/updated
- [ ] Documentation updated
- [ ] No merge conflicts
- [ ] CI checks pass

### PR Template
```markdown
## Summary
Brief description of changes

## Changes Made
- [ ] Feature/fix 1
- [ ] Feature/fix 2

## Testing
- [ ] Unit tests pass
- [ ] Manual testing completed
- [ ] Hardware testing (if applicable)

## Screenshots/Videos
(For UI changes)

## Breaking Changes
(If any)

Fixes #issue_number
```

## 🔍 Code Review Guidelines

### For Reviewers
- Be constructive and respectful
- Focus on code quality and functionality
- Check for security implications
- Verify test coverage
- Consider performance impact

### For Contributors
- Respond to feedback promptly
- Explain design decisions when asked
- Be open to suggestions
- Make requested changes in timely manner

## 🐛 Bug Report Guidelines

### Required Information
- **Device**: Make, model, Android version
- **App Version**: Version number and build flavor
- **Steps to Reproduce**: Detailed step-by-step instructions
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Logs**: Relevant error logs or crash reports

### Bug Report Template
```markdown
**Device Information**
- Device: [e.g., Samsung Galaxy S21]
- Android Version: [e.g., Android 11]
- App Version: [e.g., 1.10.000]

**Bug Description**
Clear description of the bug

**Steps to Reproduce**
1. Step 1
2. Step 2
3. Step 3

**Expected Behavior**
What you expected to happen

**Actual Behavior**
What actually happened

**Screenshots/Videos**
If applicable

**Additional Context**
Any other relevant information
```

## 🚀 Feature Request Guidelines

### Before Requesting
- Check existing issues for duplicates
- Consider if feature fits project scope
- Think about implementation complexity
- Consider backward compatibility

### Feature Request Template
```markdown
**Feature Description**
Clear description of the proposed feature

**Use Case**
Why would this feature be useful?

**Proposed Implementation**
Any ideas on how this could be implemented?

**Alternative Solutions**
Other ways to achieve the same goal

**Additional Context**
Screenshots, mockups, or examples
```

## 🌐 Localization Contributions

We welcome translations and localization improvements:

### Adding New Languages
1. Create new string resource files
2. Follow Android localization guidelines
3. Test with actual native speakers
4. Consider cultural context

### String Resource Guidelines
- Use clear, concise language
- Avoid technical jargon where possible
- Consider text expansion in other languages
- Provide context for translators

## 📞 Getting Help

### Development Questions
- **GitHub Discussions**: For general questions
- **GitHub Issues**: For bugs and feature requests
- **Email**: development@topdon.com

### Community
- Be respectful and inclusive
- Help other contributors
- Share knowledge and experience
- Follow code of conduct

## 📜 License Information

By contributing to TopInfrared, you agree that your contributions will be licensed under the project's existing license terms. See the main README for license details.

---

Thank you for contributing to TopInfrared! Your efforts help make thermal imaging technology more accessible and powerful for users worldwide.