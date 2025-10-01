# Code and Test Quality Checklist

## Code Quality
- [ ] Code follows project style guide
- [ ] No critical code smells (SonarQube/IDE inspection)
- [ ] Code coverage is at least 80% (JaCoCo)
- [ ] All public methods are tested
- [ ] No duplicated code in critical modules
- [ ] Refactoring exit criteria met (all tests pass, no new issues)

## Test Quality
- [ ] All branches and conditions are tested (branch coverage)
- [ ] Tests are independent and repeatable
- [ ] Test names describe their purpose
- [ ] Tests cover both positive and negative scenarios
- [ ] No flaky or unstable tests

## References
- SonarQube: https://www.sonarqube.org/
- JaCoCo: https://www.jacoco.org/jacoco/
- ISO/IEC 25010:2011

